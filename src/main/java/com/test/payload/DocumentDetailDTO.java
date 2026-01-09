package com.test.payload;

import com.test.enums.DocumentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentDetailDTO {
    private Long id;
    private String title;
    private String description;
    private DocumentStatus status;
    private Map<String, Object> metadata;
    private List<PieceResponseDTO> pieces;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
