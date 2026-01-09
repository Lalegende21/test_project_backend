package com.test.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentStatsDTO {
    private long totalDocuments;
    private long documentsEnCours;
    private long documentsValides;
    private long documentsBrouillon;
    private long totalPieces;
}
