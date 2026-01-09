package com.test.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "folder_contents")
@Entity
public class FolderContent extends BaseEntity {

    @NotBlank
    @Column(unique = true)
    @Size(min = 5, message = "Le nom doit être minimum de 5 caractères")
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean required;

    // Le code QR généré pour ce type de contenu
    @Column(unique = true)
    private String qrCode;

    // Relation bidirectionnelle avec les dossiers
    @ManyToMany(mappedBy = "contents", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("contents")
    private List<Folder> folders = new ArrayList<>();

    // Les pièces associées à ce type de contenu
    @OneToMany(mappedBy = "content", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("content")
    private List<Piece> pieces = new ArrayList<>();
}
