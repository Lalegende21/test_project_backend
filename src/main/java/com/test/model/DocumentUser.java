package com.test.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.test.enums.DocumentPermission;
import com.test.enums.DocumentRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "document_users")
@Entity
public class DocumentUser extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    @JsonIgnoreProperties({"users", "pieces"})
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties("documents")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentRole role;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "document_user_permissions",
            joinColumns = @JoinColumn(name = "document_user_id")
    )
    @Column(name = "permission")
    @Enumerated(EnumType.STRING)
    private Set<DocumentPermission> permissions = new HashSet<>();

    // Date à laquelle l'utilisateur a été ajouté au document
    @Column(nullable = false)
    private LocalDateTime assignedAt = LocalDateTime.now();

    // Utilisateur qui a assigné ce rôle
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_by")
    private User assignedBy;
}
