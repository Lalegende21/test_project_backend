package com.test.payload;

import com.test.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentSummaryDTO {
    private Long id;
    private String title;
    private DocumentStatus status;
    private int pieceCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
