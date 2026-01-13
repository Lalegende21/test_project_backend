package com.test.payload;

import com.test.enums.DocumentRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignUserToDocumentDTO {
    @NotNull(message = "L'ID de l'utilisateur est obligatoire")
    private Long userId;

    @NotNull(message = "Le rôle est obligatoire")
    private DocumentRole role;

    private String comment; // Commentaire sur l'assignation
}
