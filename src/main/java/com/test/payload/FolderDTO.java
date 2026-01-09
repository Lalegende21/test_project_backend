package com.test.payload;

import com.test.model.Folder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FolderDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 5, message = "Le nom doit contenir au moins 5 caractères")
    private String name;

    private String description;

    @NotNull(message = "L'ID du plan est obligatoire")
    private Long planId;

    private Long parentFolderId;
}
