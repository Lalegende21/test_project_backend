package com.test.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.test.model.Folder;
import com.test.model.FolderContent;
import com.test.repository.FolderRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RequiredArgsConstructor
@Service
public class QRCodeService {

    private final FolderRepo folderRepo;

    /**
     * Génère un QR Code contenant simplement "CONTENT:{contentId}"
     * Format simple et fiable pour la lecture
     */
    public byte[] generateQRCodeForContent(Long contentId) throws Exception {
        String qrContent = "CONTENT:" + contentId;
        return generateQRCode(qrContent, 300, 300);
    }

    /**
     * Génère un QR Code avec des dimensions personnalisables
     */
    public byte[] generateQRCode(String text, int width, int height) throws Exception {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            // Options pour améliorer la lisibilité
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Haute correction d'erreur
            hints.put(EncodeHintType.MARGIN, 1); // Marge minimale

            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();
        } catch (WriterException | IOException e) {
            throw new Exception("Erreur lors de la génération du QR Code", e);
        }
    }

    /**
     * Génère un QR Code sous forme d'image Base64 (pratique pour l'affichage direct)
     */
    public String generateQRCodeBase64(Long contentId) throws Exception {
        byte[] qrCodeBytes = generateQRCodeForContent(contentId);
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(qrCodeBytes);
    }


    public byte[] generateQRCodesZipForFolder(Long folderId) throws Exception {
        Folder folder = folderRepo.findById(folderId)
                .orElseThrow(() -> new EntityNotFoundException("Dossier non trouvé"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {

            for (FolderContent content : folder.getContents()) {
                byte[] qrCode = generateQRCodeForContent(content.getId());

                String filename = sanitizeFilename(content.getName()) + "-" + content.getId() + ".png";
                ZipEntry entry = new ZipEntry(filename);
                zos.putNextEntry(entry);
                zos.write(qrCode);
                zos.closeEntry();
            }
        }

        return baos.toByteArray();
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
}
