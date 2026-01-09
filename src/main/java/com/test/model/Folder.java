package com.test.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "folders")
@Entity
public class Folder extends BaseEntity {

    @NotBlank
    @Column(unique = true)
    @Size(min = 5, message = "Le nom doit être minimum de 5 caractères")
    private String name;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    @JsonIgnoreProperties("folders")
    private ClassificationPlan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id")
    @JsonIgnoreProperties("subFolders")
    private Folder parentFolder;

    @ToString.Exclude
    @OneToMany(mappedBy = "parentFolder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("parentFolder")
    private List<Folder> subFolders = new ArrayList<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "folder_structure_link",
            joinColumns = @JoinColumn(name = "folder_id"),
            inverseJoinColumns = @JoinColumn(name = "content_id")
    )
    @JsonIgnoreProperties("folders")
    private List<FolderContent> contents = new ArrayList<>();
}
