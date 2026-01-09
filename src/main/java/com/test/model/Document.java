package com.test.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.test.enums.DocumentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "documents")
@Entity
public class Document extends BaseEntity {

    @NotBlank
    @Column(unique = true)
    @Size(min = 5, message = "Le titre du document doit être minimum de 5 caractères")
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("document")
    private List<Piece> pieces;

    // Lien vers le FolderContent où le document est classé
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_content_id")
    private FolderContent folderContent;

    // Pour la capture collaborative (plusieurs utilisateurs)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "document_users",
            joinColumns = @JoinColumn(name = "document_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @JsonIgnoreProperties("documents")
    private List<User> users = new ArrayList<>();
}
