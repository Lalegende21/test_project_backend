package com.test.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ClassificationPlanDTO {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 3, message = "Le nom doit contenir au moins 3 caractères")
    private String name;

    private  String description;
}
