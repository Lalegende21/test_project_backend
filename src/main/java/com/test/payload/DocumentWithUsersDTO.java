package com.test.payload;

import com.test.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentWithUsersDTO {

    private Long id;
    private String title;
    private String description;
    private DocumentStatus status;
    private Map<String, Object> metadata;
    private List<PieceResponseDTO> pieces;
    private List<DocumentUserDTO> users;
    private Long createdByUserId;
    private String createdByUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
