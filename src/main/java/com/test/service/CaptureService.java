package com.test.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.test.enums.FileType;
import com.test.model.Document;
import com.test.model.FolderContent;
import com.test.model.Piece;
import com.test.repository.FolderContentRepo;
import com.test.repository.PieceRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class CaptureService {

    public static final long MAX_FILE_SIZE = 10_000_000;
    private final PieceRepo pieceRepo;
    private final FolderContentRepo contentRepo;

    @Value("${project.document}")
    private String storagePath;

    @Value("${document.base.url}")
    private String baseUrl;

    public Piece processUploadedFile(MultipartFile file, Document document) throws Exception {
        log.info("Debut du processus d'upload du fichier {}", file.getOriginalFilename());

        // Valider le fichier reçu
        validateFile(file);
        log.info("Validation du fichier reussi");

        // Lire l'image récupérée
        BufferedImage bufferedImage = readImage(file);
        log.info("Lecture de l'image");

        // Lire le QR Code depuis l'image
        String qrContent = decodeQrCode(bufferedImage);
        log.info("Lecture du QR Code sur l'image");

        // Extraire l'ID du contenu depuis le QR Code
        // Format attendu : "CONTENT:{contentId}" ou juste "{contentId}"
        Long contentId = extractContentId(qrContent);
        log.info("Extraction du contenu du QR Code sur l'image");

        // Identifier le type de contenu
        FolderContent content = contentRepo.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Type de document inconnu pour l'ID: " + contentId));
        log.info("Identification du type de contenu");

        // Génération du nom de fichier sûr
        String safeFilename = generateSafeFilename(file);
        log.info("Generation du nom du fichier");

        // Stockage du fichier
        storeFile(file, safeFilename);
        log.info("Sauvegarde du fichier");

        // Déterminer le type de fichier
        FileType fileType = determineFileType(file.getContentType());

        // Créer et sauvegarder la pièce classée automatiquement
        Piece piece = Piece.builder()
                .document(document)
                .content(content)
                .qrCodeData(qrContent)
                .fileName(file.getOriginalFilename())
                .filePath(storagePath + safeFilename)
                .pieceUrl(constructPieceUrl(safeFilename))
                .fileSize(file.getSize())
                .fileType(fileType)
                .build();

        log.info("Creation et sauvergarde de la piece");
        return this.pieceRepo.save(piece);
    }


    /**
     * Validdation du fichier.
     * Verification de fichier vide.
     * Verification de fichier non reconnu par le systeme.
     * Verification de taille de fichier superieure a la tailla maximale acceptee.
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide");
        }

        String contentType = file.getContentType();
        if (contentType == null || !List.of("image/png", "image/jpeg", "image/jpg", "image/webp", "application/pdf")
                .contains(contentType)) {
            throw new IllegalArgumentException("Type de fichier non autorisé: " + contentType);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Fichier trop volumineux (max 10MB)");
        }
    }

    private BufferedImage readImage(MultipartFile file) throws Exception {
        try (InputStream is = file.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new IllegalArgumentException("Fichier image invalide ou corrompu");
            }
            return image;
        }
    }

    private String decodeQrCode(BufferedImage image) throws Exception {
        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result result;
        try {
            result = new MultiFormatReader().decode(bitmap);
        } catch (NotFoundException e) {
            throw new IllegalArgumentException("QR Code introuvable sur l'image. Assurez-vous que l'étiquette est visible.");
        }

        return result.getText();
    }

    /**
     * Extrait l'ID du contenu depuis le QR Code
     * Format attendu : "CONTENT:123" ou juste "123"
     */
    private Long extractContentId(String qrContent) {
        try {
            // Si le format est "CONTENT:123"
            if (qrContent.startsWith("CONTENT:")) {
                return Long.parseLong(qrContent.substring(8));
            }
            // Sinon, tenter de parser directement
            return Long.parseLong(qrContent);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("QR Code invalide. Format attendu: 'CONTENT:{id}' ou '{id}'");
        }
    }

    private FileType determineFileType(String contentType) {
        if (contentType == null) return FileType.IMAGE_JPG;

        return switch (contentType) {
            case "application/pdf" -> FileType.PDF;
            case "image/png" -> FileType.IMAGE_PNG;
            case "image/jpeg", "image/jpg" -> FileType.IMAGE_JPG;
            default -> FileType.IMAGE_JPG;
        };
    }

    private String generateSafeFilename(MultipartFile file) {
        String contentType = file.getContentType();
        String extension = ".jpg"; // Par défaut

        if ("application/pdf".equals(contentType)) {
            extension = ".pdf";
        } else if ("image/png".equals(contentType)) {
            extension = ".png";
        } else if (contentType != null && contentType.startsWith("image/")) {
            extension = ".jpg";
        }

        return UUID.randomUUID() + extension;
    }

    private void storeFile(MultipartFile file, String filename) throws Exception {
        Path targetDir = Paths.get(storagePath).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);

        Path targetFile = targetDir.resolve(filename).normalize();

        // Sécurité : Vérifier que le fichier est bien dans le répertoire cible
        if (!targetFile.startsWith(targetDir)) {
            throw new SecurityException("Tentative de path traversal détectée");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String constructPieceUrl(String filename) {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        return base + "files/" + filename; // Endpoint pour servir les fichiers
    }
}
