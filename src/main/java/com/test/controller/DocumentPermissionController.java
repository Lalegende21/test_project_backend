package com.test.controller;

import com.test.model.DocumentUser;
import com.test.payload.AssignUserToDocumentDTO;
import com.test.payload.DocumentUserDTO;
import com.test.payload.MessageResponseDTO;
import com.test.payload.UpdateDocumentPermissionsDTO;
import com.test.service.DocumentPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/documents/{documentId}/users")
@RequiredArgsConstructor
public class DocumentPermissionController {

    private final DocumentPermissionService permissionService;


    @GetMapping
    public ResponseEntity<List<DocumentUserDTO>> getDocumentUsers(
            @PathVariable Long documentId,
            @AuthenticationPrincipal UserDetails userDetails) {

        List<DocumentUserDTO> users = permissionService.getDocumentUsers(documentId, userDetails.getUsername());
        return ResponseEntity.ok(users);
    }


    @PostMapping
    public ResponseEntity<DocumentUserDTO> assignUserToDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody AssignUserToDocumentDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        DocumentUser documentUser = permissionService.assignUserToDocument(
                documentId, dto, userDetails.getUsername());

        DocumentUserDTO response = DocumentUserDTO.builder()
                .id(documentUser.getId())
                .userId(documentUser.getUser().getId())
                .username(documentUser.getUser().getUsername())
                .fullName(documentUser.getUser().getFullName())
                .email(documentUser.getUser().getEmail())
                .role(documentUser.getRole())
                .permissions(documentUser.getPermissions())
                .assignedAt(documentUser.getAssignedAt())
                .assignedByUsername(documentUser.getAssignedBy().getUsername())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/{userId}")
    public ResponseEntity<DocumentUserDTO> updateUserRole(
            @PathVariable Long documentId,
            @PathVariable Long userId,
            @Valid @RequestBody UpdateDocumentPermissionsDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        dto.setUserId(userId);
        DocumentUser documentUser = permissionService.updateUserRole(
                documentId, dto, userDetails.getUsername());

        DocumentUserDTO response = DocumentUserDTO.builder()
                .id(documentUser.getId())
                .userId(documentUser.getUser().getId())
                .username(documentUser.getUser().getUsername())
                .fullName(documentUser.getUser().getFullName())
                .email(documentUser.getUser().getEmail())
                .role(documentUser.getRole())
                .permissions(documentUser.getPermissions())
                .assignedAt(documentUser.getAssignedAt())
                .build();

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{userId}")
    public ResponseEntity<MessageResponseDTO> removeUserFromDocument(
            @PathVariable Long documentId,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {

        permissionService.removeUserFromDocument(documentId, userId, userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponseDTO("Utilisateur retiré du document avec succès"));
    }
}
