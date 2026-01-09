package com.test.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentCreateDTO {
    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 3, message = "Le titre doit contenir au moins 3 caractères")
    private String title;

    private String description;

    @NotNull(message = "L'ID du dossier est obligatoire")
    private Long folderId;

    private Map<String, Object> metadata;
}
