package com.test.service;

import com.test.enums.DocumentPermission;
import com.test.enums.DocumentRole;
import com.test.enums.Role;
import com.test.model.Document;
import com.test.model.DocumentUser;
import com.test.model.User;
import com.test.payload.AssignUserToDocumentDTO;
import com.test.payload.DocumentUserDTO;
import com.test.payload.UpdateDocumentPermissionsDTO;
import com.test.repository.DocumentRepo;
import com.test.repository.DocumentUserRepo;
import com.test.repository.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentPermissionService {

    private final DocumentRepo documentRepo;
    private final DocumentUserRepo documentUserRepo;
    private final UserRepo userRepo;


    /**
     * Vérifie si un utilisateur a une permission spécifique sur un document
     */
    public boolean hasPermission(Long documentId, String username, DocumentPermission permission) {
        // Admin a toujours toutes les permissions
        User user = userRepo.findByUsername(username).orElse(null);
        if (user != null && user.getRole() == Role.ADMIN) {
            return true;
        }

        Optional<DocumentUser> documentUser = documentUserRepo.findByDocumentIdAndUsername(documentId, username);

        if (documentUser.isEmpty()) {
            return false;
        }

        return documentUser.get().getPermissions().contains(permission);
    }

    /**
     * Vérifie si un utilisateur a un rôle spécifique sur un document
     */
    public boolean hasRole(Long documentId, String username, DocumentRole role) {
        Optional<DocumentUser> documentUser = documentUserRepo.findByDocumentIdAndUsername(documentId, username);
        return documentUser.isPresent() && documentUser.get().getRole() == role;
    }

    /**
     * Vérifie si l'utilisateur est le propriétaire du document
     */
    public boolean isOwner(Long documentId, String username) {
        Document document = documentRepo.findById(documentId).orElse(null);
        return document != null && document.getCreatedBy().getUsername().equals(username);
    }


    /**
     * Assigne un utilisateur à un document avec un rôle
     */
    public DocumentUser assignUserToDocument(Long documentId, AssignUserToDocumentDTO dto, String assignedByUsername) {
        // Vérifier que l'utilisateur qui assigne a la permission MANAGE
        if (!hasPermission(documentId, assignedByUsername, DocumentPermission.MANAGE)) {
            throw new SecurityException("Vous n'avez pas la permission de gérer les utilisateurs de ce document");
        }

        Document document = documentRepo.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document non trouvé"));

        User userToAssign = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        User assignedBy = userRepo.findByUsername(assignedByUsername)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur assignant non trouvé"));

        // Vérifier si l'utilisateur est déjà assigné
        Optional<DocumentUser> existing = documentUserRepo.findByDocumentIdAndUserId(documentId, dto.getUserId());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Cet utilisateur est déjà assigné à ce document");
        }

        // Créer l'assignation
        DocumentUser documentUser = DocumentUser.builder()
                .document(document)
                .user(userToAssign)
                .role(dto.getRole())
                .permissions(dto.getRole().getPermissions())
                .assignedAt(LocalDateTime.now())
                .assignedBy(assignedBy)
                .build();

        return documentUserRepo.save(documentUser);
    }

    /**
     * Met à jour le rôle d'un utilisateur sur un document
     */
    public DocumentUser updateUserRole(Long documentId, UpdateDocumentPermissionsDTO dto, String updatedByUsername) {
        // Vérifier les permissions
        if (!hasPermission(documentId, updatedByUsername, DocumentPermission.MANAGE)) {
            throw new SecurityException("Vous n'avez pas la permission de modifier les rôles");
        }

        DocumentUser documentUser = documentUserRepo.findByDocumentIdAndUserId(documentId, dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Assignation non trouvée"));

        // Ne pas permettre de modifier le rôle du propriétaire
        if (documentUser.getRole() == DocumentRole.OWNER) {
            throw new IllegalArgumentException("Impossible de modifier le rôle du propriétaire");
        }

        documentUser.setRole(dto.getRole());
        documentUser.setPermissions(dto.getRole().getPermissions());

        return documentUserRepo.save(documentUser);
    }


    /**
     * Retire un utilisateur d'un document
     */
    public void removeUserFromDocument(Long documentId, Long userId, String removedByUsername) {
        // Vérifier les permissions
        if (!hasPermission(documentId, removedByUsername, DocumentPermission.MANAGE)) {
            throw new SecurityException("Vous n'avez pas la permission de retirer des utilisateurs");
        }

        DocumentUser documentUser = documentUserRepo.findByDocumentIdAndUserId(documentId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Assignation non trouvée"));

        // Ne pas permettre de retirer le propriétaire
        if (documentUser.getRole() == DocumentRole.OWNER) {
            throw new IllegalArgumentException("Impossible de retirer le propriétaire du document");
        }

        documentUserRepo.delete(documentUser);
    }


    /**
     * Récupère tous les utilisateurs assignés à un document
     */
    public List<DocumentUserDTO> getDocumentUsers(Long documentId, String requestUsername) {
        // Vérifier que l'utilisateur a accès au document
        if (!hasPermission(documentId, requestUsername, DocumentPermission.VIEW)) {
            throw new SecurityException("Vous n'avez pas accès à ce document");
        }

        List<DocumentUser> documentUsers = documentUserRepo.findByDocumentId(documentId);

        return documentUsers.stream()
                .map(du -> DocumentUserDTO.builder()
                        .id(du.getId())
                        .userId(du.getUser().getId())
                        .username(du.getUser().getUsername())
                        .fullName(du.getUser().getFullName())
                        .email(du.getUser().getEmail())
                        .role(du.getRole())
                        .permissions(du.getPermissions())
                        .assignedAt(du.getAssignedAt())
                        .assignedByUsername(du.getAssignedBy() != null ? du.getAssignedBy().getUsername() : null)
                        .build())
                .collect(Collectors.toList());
    }


    /**
     * Récupère tous les documents d'un utilisateur
     */
    public List<Document> getUserDocuments(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur non trouvé"));

        List<DocumentUser> documentUsers = documentUserRepo.findByUserId(user.getId());

        return documentUsers.stream()
                .map(DocumentUser::getDocument)
                .collect(Collectors.toList());
    }
}
