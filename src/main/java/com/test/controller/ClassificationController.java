package com.test.controller;

import com.test.model.ClassificationPlan;
import com.test.model.Folder;
import com.test.model.FolderContent;
import com.test.payload.*;
import com.test.service.ClassificationService;
import com.test.service.QRCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/classification")
@RestController
public class ClassificationController {

    private final ClassificationService classificationService;
    private final QRCodeService qrCodeService;


    // ============== GESTION DES PLANS ==============

    @PostMapping("/plans")
    public ResponseEntity<ClassificationPlan> createPlan(@Valid @RequestBody ClassificationPlanDTO classificationPlanDTO) {
        ClassificationPlan classificationPlan = classificationService.createPlan(classificationPlanDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(classificationPlan);
    }

    @GetMapping("/plans")
    public ResponseEntity<List<ClassificationPlan>> getAllPlans() {
        List<ClassificationPlan> plans = classificationService.getAllPlans();
        return ResponseEntity.status(HttpStatus.OK).body(plans);
    }

    @GetMapping("/plans/{id}")
    public ResponseEntity<ClassificationPlan> getPlanById(@PathVariable Long id) {
        ClassificationPlan plan = classificationService.getPlanById(id);
        return ResponseEntity.status(HttpStatus.OK).body(plan);
    }

    @GetMapping("/plans/{id}/tree")
    public ResponseEntity<List<FolderNodeDTO>> getPlanTree(@PathVariable Long id) {
        List<FolderNodeDTO> tree = classificationService.getPlanTree(id);
        return ResponseEntity.ok(tree);
    }

    @DeleteMapping("/plans/{id}")
    public ResponseEntity<MessageResponseDTO> deletePlan(@PathVariable Long id) {
        classificationService.deletePlan(id);
        return ResponseEntity.ok(new MessageResponseDTO("Plan supprimé avec succès"));
    }



    // ============== GESTION DES DOSSIERS ==============

    // Gestion des dossiers & parentId est optionnel : si absent, c'est un dossier racine du plan
    @PostMapping("/folders")
    public ResponseEntity<Folder> createFolder(@RequestBody FolderDTO folderDTO) {
        Folder folder = classificationService.createFolder(folderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(folder);
    }

    @GetMapping("/folders/{id}")
    public ResponseEntity<Folder> getFolderById(@PathVariable Long id) {
        Folder folder = classificationService.getFolderById(id);
        return ResponseEntity.ok(folder);
    }

    @PutMapping("/folders/{id}")
    public ResponseEntity<Folder> updateFolder(
            @PathVariable Long id,
            @Valid @RequestBody FolderDTO dto) {
        Folder folder = classificationService.updateFolder(id, dto);
        return ResponseEntity.ok(folder);
    }

    @DeleteMapping("/folders/{id}")
    public ResponseEntity<MessageResponseDTO> deleteFolder(@PathVariable Long id) {
        classificationService.deleteFolder(id);
        return ResponseEntity.ok(new MessageResponseDTO("Dossier supprimé avec succès"));
    }



    // ============== GESTION DES CONTENUS ==============

    @PostMapping("/contents")
    public ResponseEntity<FolderContent> createContent(@Valid @RequestBody FolderContentDTO dto) throws Exception {
        FolderContent content = classificationService.createContent(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(content);
    }

    @GetMapping("/contents/{id}")
    public ResponseEntity<FolderContent> getContentById(@PathVariable Long id) {
        FolderContent content = classificationService.getContentById(id);
        return ResponseEntity.ok(content);
    }

    @PutMapping("/contents/{id}")
    public ResponseEntity<FolderContent> updateContent(
            @PathVariable Long id,
            @Valid @RequestBody FolderContentDTO dto) {
        FolderContent content = classificationService.updateContent(id, dto);
        return ResponseEntity.ok(content);
    }

    @DeleteMapping("/contents/{id}")
    public ResponseEntity<MessageResponseDTO> deleteContent(@PathVariable Long id) {
        classificationService.deleteContent(id);
        return ResponseEntity.ok(new MessageResponseDTO("Contenu supprimé avec succès"));
    }



    // ============== LIAISON CONTENU-DOSSIER ==============
    // Lier un type de contenu à un dossier spécifique
    @PostMapping("/folders/{folderId}/contents/{contentId}")
    public ResponseEntity<?> createFolderContent(@PathVariable Long folderId,
                                                 @PathVariable Long contentId) {
        classificationService.linkContentToFolder(folderId, contentId);
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseDTO("Contenu lié au dossier avec succès"));
    }

    @GetMapping("/folders/{folderId}/contents")
    public ResponseEntity<List<FolderContent>> getFolderContents(@PathVariable Long folderId) {
        List<FolderContent> contents = classificationService.getFolderContents(folderId);
        return ResponseEntity.ok(contents);
    }

    @DeleteMapping("/folders/{folderId}/contents/{contentId}")
    public ResponseEntity<MessageResponseDTO> unlinkContentFromFolder(
            @PathVariable Long folderId,
            @PathVariable Long contentId) {
        classificationService.unlinkContentFromFolder(folderId, contentId);
        return ResponseEntity.ok(new MessageResponseDTO("Contenu dissocié du dossier avec succès"));
    }



    // ============== GÉNÉRATION DE QR CODES ==============

    @GetMapping("/contents/{id}/qrcode")
    public ResponseEntity<byte[]> getQRCode(@PathVariable Long id) throws Exception {
        byte[] qrCode = qrCodeService.generateQRCodeForContent(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDisposition(
                ContentDisposition.builder("inline")
                        .filename("qrcode-content-" + id + ".png")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(qrCode);
    }

    @GetMapping("/contents/{id}/qrcode/base64")
    public ResponseEntity<QRCodeResponseDTO> getQRCodeBase64(@PathVariable Long id) throws Exception {
        String base64QrCode = qrCodeService.generateQRCodeBase64(id);
        FolderContent content = classificationService.getContentById(id);

        QRCodeResponseDTO response = QRCodeResponseDTO.builder()
                .contentId(id)
                .contentName(content.getName())
                .qrCodeBase64(base64QrCode)
                .qrCodeData(content.getQrCode())
                .build();

        return ResponseEntity.ok(response);
    }

    // Télécharger tous les QR codes d'un dossier sous forme de ZIP
    @GetMapping("/folders/{folderId}/qrcodes/download")
    public ResponseEntity<byte[]> downloadFolderQRCodes(@PathVariable Long folderId) throws Exception {
        byte[] zipFile = qrCodeService.generateQRCodesZipForFolder(folderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(
                ContentDisposition.builder("attachment")
                        .filename("qrcodes-folder-" + folderId + ".zip")
                        .build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(zipFile);
    }
}
