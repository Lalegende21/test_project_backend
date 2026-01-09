package com.test.controller;

import com.test.enums.DocumentStatus;
import com.test.model.Document;
import com.test.model.Piece;
import com.test.payload.*;
import com.test.service.CaptureService;
import com.test.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RequestMapping("/documents")
@RestController
public class DocumentController {

    private final DocumentService  documentService;
    private final CaptureService  captureService;


    // ============== CRÉATION DE DOCUMENT ==============

    @PostMapping
    public ResponseEntity<Document> createDocument(@Valid @RequestBody DocumentCreateDTO payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.documentService.createDocument(payload));
    }



    // ============== UPLOAD DE PIÈCES ==============

    @PostMapping("/{documentId}/pieces")
    public ResponseEntity<PieceResponseDTO> uploadPiece(
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file) {

        try {
            Piece piece = documentService.uploadAndClassifyPiece(documentId, file);

            PieceResponseDTO response = PieceResponseDTO.builder()
                    .id(piece.getId())
                    .fileName(piece.getFileName())
                    .fileSize(piece.getFileSize())
                    .fileType(piece.getFileType())
                    .pieceUrl(piece.getPieceUrl())
                    .qrCodeData(piece.getQrCodeData())
                    .contentId(piece.getContent().getId())
                    .contentName(piece.getContent().getName())
                    .isRequired(piece.getContent().isRequired())
                    .createdAt(piece.getCreatedAt())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Erreur lors du traitement du fichier: " + e.getMessage());
        }
    }

    // Upload multiple de pièces
    @PostMapping("/{documentId}/pieces/batch")
    public ResponseEntity<List<PieceResponseDTO>> uploadMultiplePieces(
            @PathVariable Long documentId,
            @RequestParam("files") List<MultipartFile> files) {

        List<PieceResponseDTO> responses = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            try {
                Piece piece = documentService.uploadAndClassifyPiece(documentId, files.get(i));

                PieceResponseDTO response = PieceResponseDTO.builder()
                        .id(piece.getId())
                        .fileName(piece.getFileName())
                        .fileSize(piece.getFileSize())
                        .fileType(piece.getFileType())
                        .pieceUrl(piece.getPieceUrl())
                        .contentId(piece.getContent().getId())
                        .contentName(piece.getContent().getName())
                        .build();

                responses.add(response);

            } catch (Exception e) {
                errors.add("Fichier " + (i + 1) + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty() && responses.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Aucun fichier n'a pu être traité: " + String.join(", ", errors));
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }



    // ============== CONSULTATION DE DOCUMENTS ==============

    @GetMapping("/{id}")
    public ResponseEntity<DocumentDetailDTO> getDocument(@PathVariable Long id) {
        Document document = documentService.getDocumentForValidation(id);

        DocumentDetailDTO dto = DocumentDetailDTO.builder()
                .id(document.getId())
                .title(document.getTitle())
                .description(document.getDescription())
                .status(document.getStatus())
                .metadata(document.getMetadata())
                .pieces(document.getPieces().stream()
                        .map(p -> PieceResponseDTO.builder()
                                .id(p.getId())
                                .fileName(p.getFileName())
                                .fileSize(p.getFileSize())
                                .fileType(p.getFileType())
                                .pieceUrl(p.getPieceUrl())
                                .contentId(p.getContent().getId())
                                .contentName(p.getContent().getName())
                                .isRequired(p.getContent().isRequired())
                                .createdAt(p.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();

        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<DocumentSummaryDTO>> getAllDocuments(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) DocumentStatus status) {

        List<Document> documents = documentService.searchDocuments(title, status);

        List<DocumentSummaryDTO> summaries = documents.stream()
                .map(doc -> DocumentSummaryDTO.builder()
                        .id(doc.getId())
                        .title(doc.getTitle())
                        .status(doc.getStatus())
                        .pieceCount(doc.getPieces().size())
                        .createdAt(doc.getCreatedAt())
                        .updatedAt(doc.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(summaries);
    }



    // ============== VALIDATION DE DOCUMENT ==============

    @PostMapping("/{id}/validate")
    public ResponseEntity<DocumentValidationResponseDTO> validateDocument(@PathVariable Long id) {
        DocumentValidationResponseDTO response = documentService.validateDocument(id);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
        }
    }



    // ============== MISE À JOUR DE DOCUMENT ==============

    @PutMapping("/{id}")
    public ResponseEntity<Document> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentUpdateDTO dto) {
        Document document = documentService.updateDocument(id, dto);
        return ResponseEntity.ok(document);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Document> updateDocumentStatus(
            @PathVariable Long id,
            @RequestParam DocumentStatus status) {
        Document document = documentService.updateDocumentStatus(id, status);
        return ResponseEntity.ok(document);
    }



    // ============== SUPPRESSION ==============

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(new MessageResponseDTO("Document supprimé avec succès"));
    }

    @DeleteMapping("/{documentId}/pieces/{pieceId}")
    public ResponseEntity<MessageResponseDTO> deletePiece(
            @PathVariable Long documentId,
            @PathVariable Long pieceId) {
        documentService.deletePiece(documentId, pieceId);
        return ResponseEntity.ok(new MessageResponseDTO("Pièce supprimée avec succès"));
    }



    // ============== STATISTIQUES ==============

    @GetMapping("/stats")
    public ResponseEntity<DocumentStatsDTO> getDocumentStats() {
        DocumentStatsDTO stats = documentService.getDocumentStatistics();
        return ResponseEntity.ok(stats);
    }
}
