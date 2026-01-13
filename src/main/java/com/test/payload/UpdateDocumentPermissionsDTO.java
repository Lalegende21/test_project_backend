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
public class UpdateDocumentPermissionsDTO {

    @NotNull
    private Long userId;

    @NotNull
    private DocumentRole role;
}
