package com.test.service;

import com.test.enums.DocumentPermission;
import com.test.enums.DocumentRole;
import com.test.enums.DocumentStatus;
import com.test.model.*;
import com.test.payload.*;
import com.test.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
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
    private final UserRepo userRepo;
    private final DocumentPermissionService permissionService;


    public Document createDocument(DocumentCreateDTO payload, String username) {
        log.info("Debut de la creation du document {}", payload);

        // Récupérer l'utilisateur connecté
        User currentUser = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

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
                .createdBy(currentUser)
                .documentUsers(new ArrayList<>())
                .build();

        document = documentRepo.save(document);

        // Créer automatiquement l'assignation OWNER pour le créateur
        DocumentUser ownerAssignment = DocumentUser.builder()
                .document(document)
                .user(currentUser)
                .role(DocumentRole.OWNER)
                .permissions(DocumentRole.OWNER.getPermissions())
                .assignedAt(LocalDateTime.now())
                .assignedBy(currentUser)
                .build();

        log.info("Sauvegarde et fin de la creation du document");
        return this.documentRepo.save(document);
    }


    public Piece uploadAndClassifyPiece(Long documentId, MultipartFile file, String username) throws Exception {
        log.info("Debut de l'upload et de la classification des pieces");

        // Vérifier la permission UPLOAD
        if (!permissionService.hasPermission(documentId, username, DocumentPermission.UPLOAD)) {
            throw new SecurityException("Vous n'avez pas la permission d'ajouter des pièces à ce document");
        }

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


    public Document getDocumentForValidation(Long id, String username) {
        // Vérifier la permission VIEW
        if (!permissionService.hasPermission(id, username, DocumentPermission.VIEW)) {
            throw new SecurityException("Vous n'avez pas accès à ce document");
        }

        return this.documentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document non trouvé"));
    }


    /**
     * Valider un document en vérifiant que toutes les pièces obligatoires sont présentes
     */
    public DocumentValidationResponseDTO validateDocument(Long id, String username) {
        // Vérifier la permission VALIDATE
        if (!permissionService.hasPermission(id, username, DocumentPermission.VALIDATE)) {
            throw new SecurityException("Vous n'avez pas la permission de valider ce document");
        }

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

    public List<Document> searchDocuments(String title, DocumentStatus status, String username) {
        // Récupérer les documents de l'utilisateur
        List<Document> userDocuments = permissionService.getUserDocuments(username);

        // Appliquer les filtres
        return userDocuments.stream()
                .filter(doc -> title == null || doc.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(doc -> status == null || doc.getStatus() == status)
                .collect(Collectors.toList());
    }


    public Document updateDocument(Long id, DocumentUpdateDTO dto, String username) {
        // Vérifier la permission EDIT
        if (!permissionService.hasPermission(id, username, DocumentPermission.EDIT)) {
            throw new SecurityException("Vous n'avez pas la permission de modifier ce document");
        }

        Document document = getDocumentForValidation(id, username);

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

    public Document updateDocumentStatus(Long id, String username, DocumentStatus status) {
        Document document = getDocumentForValidation(id, username);
        document.setStatus(status);
        return documentRepo.save(document);
    }


    public void deleteDocument(Long id, String username) {
        // Vérifier la permission MANAGE (seul le owner peut supprimer)
        if (!permissionService.hasPermission(id, username, DocumentPermission.MANAGE)) {
            throw new SecurityException("Seul le propriétaire peut supprimer ce document");
        }

        Document document = getDocumentForValidation(id, username);
        documentRepo.delete(document);
    }


    public void deletePiece(Long documentId, Long pieceId, String username) {
        // Vérifier la permission DELETE
        if (!permissionService.hasPermission(documentId, username, DocumentPermission.DELETE)) {
            throw new SecurityException("Vous n'avez pas la permission de supprimer des pièces");
        }

        Document document = getDocumentForValidation(documentId, username);
        Piece piece = document.getPieces().stream()
                .filter(p -> p.getId().equals(pieceId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Pièce non trouvée"));

        document.getPieces().remove(piece);
        documentRepo.save(document);
    }

    public DocumentStatsDTO getDocumentStatistics(String username) {
        User user = userRepo.findByUsername(username).orElseThrow();

        List<Document> userDocs = permissionService.getUserDocuments(username);

        long total = userDocs.size();
        long enCours = userDocs.stream().filter(d -> d.getStatus() == DocumentStatus.EN_COURS).count();
        long valides = userDocs.stream().filter(d -> d.getStatus() == DocumentStatus.VALIDE).count();
        long brouillon = userDocs.stream().filter(d -> d.getStatus() == DocumentStatus.BROUILLON).count();
        long totalPieces = userDocs.stream().mapToLong(d -> d.getPieces().size()).sum();

        return DocumentStatsDTO.builder()
                .totalDocuments(total)
                .documentsEnCours(enCours)
                .documentsValides(valides)
                .documentsBrouillon(brouillon)
                .totalPieces(totalPieces)
                .build();
    }


    public DocumentWithUsersDTO getDocumentWithUsers(Long id, String username) {
        if (!permissionService.hasPermission(id, username, DocumentPermission.VIEW)) {
            throw new SecurityException("Vous n'avez pas accès à ce document");
        }

        Document doc = documentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Document non trouvé"));

        List<DocumentUserDTO> users = permissionService.getDocumentUsers(id, username);

        return DocumentWithUsersDTO.builder()
                .id(doc.getId())
                .title(doc.getTitle())
                .description(doc.getDescription())
                .status(doc.getStatus())
                .metadata(doc.getMetadata())
                .pieces(doc.getPieces().stream()
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
                .users(users)
                .createdByUserId(doc.getCreatedBy().getId())
                .createdByUsername(doc.getCreatedBy().getUsername())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .build();
    }
}
