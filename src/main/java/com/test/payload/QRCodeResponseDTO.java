package com.test.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class QRCodeResponseDTO {
    private Long contentId;
    private String contentName;
    private String qrCodeBase64;
    private String qrCodeData;
}
