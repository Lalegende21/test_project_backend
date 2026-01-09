package com.test.service;

import com.test.enums.DocumentStatus;
import com.test.model.Document;
import com.test.model.Folder;
import com.test.model.FolderContent;
import com.test.model.Piece;
import com.test.payload.*;
import com.test.repository.DocumentRepo;
import com.test.repository.FolderContentRepo;
import com.test.repository.FolderRepo;
import com.test.repository.PieceRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DocumentService {

    private final DocumentRepo documentRepo;
    private final CaptureService captureService;
    private final FolderRepo folderRepo;
    private final FolderContentRepo folderContentRepo;
    private final PieceRepo pieceRepo;


    public Document createDocument(DocumentCreateDTO payload) {
        log.info("Debut de la creation du document {}", payload);

        // Valider que le dossier existe
        if (payload.getFolderId() == null) {
            log.error("L'ID du dossier est obligatoire");
            throw new IllegalArgumentException("L'ID du dossier est obligatoire");
        }

        // Vérifier que le dossier existe réellement
        Folder folder = folderRepo.findById(payload.getFolderId())
                .orElseThrow(() -> new EntityNotFoundException("Dossier non trouvé"));
        log.info("Verification de l'existance du dossier");

        // Initialiser metadata si null
        Map<String, Object> metadata = payload.getMetadata();
        if (metadata == null) {
            metadata = new HashMap<>();
        }

        // Ajouter folderId dans les métadonnées
        metadata.put("folderId", payload.getFolderId());
        metadata.put("folderName", folder.getName());

        Document document = Document.builder()
                .title(payload.getTitle())
                .description(payload.getDescription())
                .metadata(metadata)
                .status(DocumentStatus.BROUILLON)
                .build();

        log.info("Sauvegarde et fin de la creation du document");
        return this.documentRepo.save(document);
    }


    public Piece uploadAndClassifyPiece(Long documentId, MultipartFile file) throws Exception {
        log.info("Debut de l'upload et de la classification des pieces");

        // Verifier si le document existe
        Document document = this.documentRepo.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document non trouvé"));
        log.info("Verification de l'existence du document");

        // Vérifier le statut du document
        if (document.getStatus() == DocumentStatus.VALIDE) {
            log.error("Impossibilité d'ajouter des pièces à un document validé");
            throw new IllegalStateException("Impossible d'ajouter des pièces à un document validé");
        }

        // Appeler le service pour traiter le QR Code et enregistrer le fichier
        log.info("Traitement du QR code et de la sauvegarde des pieces");
        Piece piece = this.captureService.processUploadedFile(file, document);

        // Mettre le document en cours
        if (document.getStatus() == DocumentStatus.BROUILLON) {
            document.setStatus(DocumentStatus.EN_COURS);
            documentRepo.save(document);
        }
        log.info("Modification du status des documents et fin du processus");

        return piece;
    }


    public Document getDocumentForValidation(Long id) {
        return this.documentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document non trouvé"));
    }


    /**
     * Valider un document en vérifiant que toutes les pièces obligatoires sont présentes
     */
    public DocumentValidationResponseDTO validateDocument(Long id) {
        Document doc = this.documentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document non trouvé"));

        // Récupérer l'ID du dossier depuis les métadonnées
        Object folderIdObj = doc.getMetadata().get("folderId");
        if (folderIdObj == null) {
            throw new IllegalStateException("L'ID du dossier est manquant dans les métadonnées");
        }

        Long folderId = Long.valueOf(folderIdObj.toString());

        // IDs des contenus déjà capturés
        Set<Long> capturedContentIds = doc.getPieces().stream()
                .map(p -> p.getContent().getId())
                .collect(Collectors.toSet());

        // Récupérer les contenus obligatoires pour ce dossier
        List<FolderContent> requiredContents = folderContentRepo.findRequiredContentsByFolderId(folderId);

        List<FolderContent> missingContents = requiredContents.stream()
                .filter(fc -> !capturedContentIds.contains(fc.getId()))
                .collect(Collectors.toList());

        // Si des pièces obligatoires manquent
        if (!missingContents.isEmpty()) {
            List<String> missingNames = missingContents.stream()
                    .map(FolderContent::getName)
                    .collect(Collectors.toList());

            return DocumentValidationResponseDTO.builder()
                    .success(false)
                    .message("Certaines pièces obligatoires manquent")
                    .missingContents(missingNames)
                    .build();
        }

        // Validation réussie
        doc.setStatus(DocumentStatus.VALIDE);
        documentRepo.save(doc);

        return DocumentValidationResponseDTO.builder()
                .success(true)
                .message("Document validé avec succès")
                .build();
    }

    public List<Document> searchDocuments(String title, DocumentStatus status) {
        // Implémentation basique de recherche
        if (title != null && status != null) {
            return documentRepo.findByTitleContainingIgnoreCaseAndStatus(title, status);
        } else if (title != null) {
            return documentRepo.findByTitleContainingIgnoreCase(title);
        } else if (status != null) {
            return documentRepo.findByStatus(status);
        }
        return documentRepo.findAll();
    }


    public Document updateDocument(Long id, DocumentUpdateDTO dto) {
        Document document = getDocumentForValidation(id);

        if (dto.getTitle() != null) {
            document.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            document.setDescription(dto.getDescription());
        }
        if (dto.getMetadata() != null) {
            document.setMetadata(dto.getMetadata());
        }

        return documentRepo.save(document);
    }

    public Document updateDocumentStatus(Long id, DocumentStatus status) {
        Document document = getDocumentForValidation(id);
        document.setStatus(status);
        return documentRepo.save(document);
    }


    public void deleteDocument(Long id) {
        Document document = getDocumentForValidation(id);
        documentRepo.delete(document);
    }


    public void deletePiece(Long documentId, Long pieceId) {
        Document document = getDocumentForValidation(documentId);
        Piece piece = document.getPieces().stream()
                .filter(p -> p.getId().equals(pieceId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Pièce non trouvée"));

        document.getPieces().remove(piece);
        documentRepo.save(document);
    }

    public DocumentStatsDTO getDocumentStatistics() {
        long total = documentRepo.count();
        long enCours = documentRepo.countByStatus(DocumentStatus.EN_COURS);
        long valides = documentRepo.countByStatus(DocumentStatus.VALIDE);
        long brouillon = documentRepo.countByStatus(DocumentStatus.BROUILLON);
        long totalPieces = pieceRepo.count();

        return DocumentStatsDTO.builder()
                .totalDocuments(total)
                .documentsEnCours(enCours)
                .documentsValides(valides)
                .documentsBrouillon(brouillon)
                .totalPieces(totalPieces)
                .build();
    }
}
