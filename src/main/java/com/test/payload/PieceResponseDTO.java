package com.test.payload;

import com.test.enums.FileType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class PieceResponseDTO {
    private Long id;
    private String fileName;
    private Long fileSize;
    private FileType fileType;
    private String pieceUrl;
    private String qrCodeData;
    private Long contentId;
    private String contentName;
    private boolean isRequired;
    private LocalDateTime createdAt;
}
