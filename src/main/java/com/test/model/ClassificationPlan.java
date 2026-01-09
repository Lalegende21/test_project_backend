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
@Table(name = "classification_plans")
@Entity
public class ClassificationPlan extends BaseEntity {

    @NotBlank
    @Column(unique = true)
    @Size(min = 5, message = "Le nom doit être minimum de 5 caractères")
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "plan", cascade =  CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("plan")
    private List<Folder> folders = new ArrayList<>();
}
