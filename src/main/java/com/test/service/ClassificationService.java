package com.test.service;

import com.test.model.ClassificationPlan;
import com.test.model.Folder;
import com.test.model.FolderContent;
import com.test.payload.ClassificationPlanDTO;
import com.test.payload.FolderContentDTO;
import com.test.payload.FolderDTO;
import com.test.payload.FolderNodeDTO;
import com.test.repository.ClassificationRepo;
import com.test.repository.FolderContentRepo;
import com.test.repository.FolderRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class ClassificationService {

    private final ClassificationRepo classificationRepo;
    private final FolderRepo folderRepo;
    private final FolderContentRepo  folderContentRepo;
    private final QRCodeService qrCodeService;


    // ============== GESTION DES PLANS ==============
    // Methode de creation des plans
    public ClassificationPlan createPlan(ClassificationPlanDTO classificationPlanDTO) {
        log.info("Debut de la creation du plan {}", classificationPlanDTO);

        // Vérifier si un plan avec ce nom existe déjà
        if (classificationRepo.existsByName(classificationPlanDTO.getName())) {
            log.error("Un plan avec ce nom: {}, existe déjà",  classificationPlanDTO.getName());
            throw new IllegalArgumentException("Un plan avec ce nom existe déjà");
        }

        ClassificationPlan classificationPlan = ClassificationPlan.builder()
                .name(classificationPlanDTO.getName())
                .description(classificationPlanDTO.getDescription())
                .active(true)
                .build();

        log.info("Sauvegarde et fin de la creation du plan {}", classificationPlan);
        return  classificationRepo.save(classificationPlan);
    }

    // Methode de recuperation de tous les plans
    public List<ClassificationPlan> getAllPlans() {
        return classificationRepo.findAll();
    }

    // Methode de recuperation d'un plan par identifiant
    public ClassificationPlan getPlanById(Long id) {
        return classificationRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Plan de classification non trouvé"));
    }

    // Methode de suppression d'un plan par identifiant
    public void deletePlan(Long id) {
        ClassificationPlan plan = getPlanById(id);
        classificationRepo.delete(plan);
    }


    // ============== GESTION DES DOSSIERS (Elements constitutifs) ==============
    // Methode de creation d'un contenu
    public FolderContent createContent(FolderContentDTO folderContentDTO) {
        log.info("Debut de la creation du contenu {}", folderContentDTO);

        // Vérifier si un folderContent avec ce nom existe déjà
        if (folderContentRepo.existsByName(folderContentDTO.getName())) {
            log.error("Un contenu avec ce nom: {}, existe déjà",  folderContentDTO.getName());
            throw new IllegalArgumentException("Un contenu avec ce nom existe déjà");
        }

        FolderContent folderContent = FolderContent.builder()
                .name(folderContentDTO.getName())
                .description(folderContentDTO.getDescription())
                .required(folderContentDTO.isRequired())
                .build();

        // Sauvegarder d'abord pour obtenir l'ID
        folderContent = folderContentRepo.save(folderContent);
        log.info("Sauvegarde du contenu {}", folderContent);

        // CRITIQUE : Générer le QR Code automatiquement
        // On ne peut pas le stocker comme byte[], donc on stocke juste l'identifiant
        folderContent.setQrCode("CONTENT:" + folderContent.getId());

        return  folderContentRepo.save(folderContent);
    }

    public FolderContent getContentById(Long id) {
        return folderContentRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Contenu non trouvé"));
    }

    public FolderContent updateContent(Long id, FolderContentDTO dto) {
        FolderContent content = getContentById(id);
        content.setName(dto.getName());
        content.setDescription(dto.getDescription());
        content.setRequired(dto.isRequired());
        return folderContentRepo.save(content);
    }

    public void deleteContent(Long id) {
        FolderContent content = getContentById(id);
        folderContentRepo.delete(content);
    }

    public List<FolderContent> getFolderContents(Long folderId) {
        Folder folder = getFolderById(folderId);
        return folder.getContents();
    }



    // ============== GESTION DES DOSSIERS ==============

    public Folder createFolder(FolderDTO folderDTO) {
        log.info("Debut de la creation du dossier {}", folderDTO);

        // Vérifier si un folder avec ce nom existe déjà
        if (folderRepo.existsByName(folderDTO.getName())) {
            log.error("Un dossier avec ce nom: {}, existe déjà",  folderDTO.getName());
            throw new IllegalArgumentException("Un dossier avec ce nom existe déjà");
        }

        // Recherche du plan à rattacher
        log.info("Recherche du plan a rattacher au dossier");
        ClassificationPlan plan = classificationRepo.findById(folderDTO.getPlanId())
                .orElseThrow(() -> new EntityNotFoundException("Plan de classification non trouvé"));

        Folder folder = Folder.builder()
                .name(folderDTO.getName())
                .description(folderDTO.getDescription())
                .plan(plan)
                .build();

        // Verifier si c'est un sous-dossier
        log.info("Verifier si le dossier est un sous-dossier");
        if (folderDTO.getParentFolderId() != null) {
            Folder parent = folderRepo.findById(folderDTO.getParentFolderId())
                    .orElseThrow(() -> new EntityNotFoundException("Dossier parent non trouvé"));

            // Vérifier que le parent appartient au même plan
            if (!parent.getPlan().getId().equals(plan.getId())) {
                throw new IllegalArgumentException("Le dossier parent doit appartenir au même plan");
            }

            folder.setParentFolder(parent);
        }

        log.info("Fin et sauvegarde du dossier {}", folder);
        return  folderRepo.save(folder);
    }


    public Folder getFolderById(Long id) {
        return folderRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Dossier non trouvé"));
    }

    public Folder updateFolder(Long id, FolderDTO dto) {
        Folder folder = getFolderById(id);
        folder.setName(dto.getName());
        folder.setDescription(dto.getDescription());
        return folderRepo.save(folder);
    }

    public void deleteFolder(Long id) {
        Folder folder = getFolderById(id);
        folderRepo.delete(folder);
    }


    // Lier un contenu a un dossier (Un contenu peut etre dans plusieurs dossiers)
    public void linkContentToFolder(Long folderId, Long contentId) {
        log.info("Debut de la liaison du contenu à un dossier");

        Folder folder = folderRepo.findById(folderId)
                .orElseThrow(() -> new EntityNotFoundException("Dossier non trouvé"));
        log.info("Recherche du dossier");

        FolderContent content = folderContentRepo.findById(contentId)
                .orElseThrow(() -> new EntityNotFoundException("Contenu à inserer non trouvé"));
        log.info("Recherche du contenu");

        // Vérifier si le contenu n'est pas déjà lié
        if (folder.getContents().contains(content)) {
            log.error("Contenu déjà lié à un dossier");
            throw new IllegalArgumentException("Ce contenu est déjà lié à ce dossier");
        }

        folder.getContents().add(content);

        log.info("Fin et sauvegarde de la liaison du contenu à un dossier");
        folderRepo.save(folder);
    }

    public void unlinkContentFromFolder(Long folderId, Long contentId) {
        Folder folder = getFolderById(folderId);
        FolderContent content = getContentById(contentId);
        folder.getContents().remove(content);
        folderRepo.save(folder);
    }


    // Récupérer uniquement les dossiers racines du plan (ceux qui n'ont pas de parent)
    public List<FolderNodeDTO> getPlanTree(Long planId) {
        // Vérifier que le plan existe
        if (!classificationRepo.existsById(planId)) {
            throw new EntityNotFoundException("Plan de classification non trouvé");
        }

        List<Folder> rootFolders = folderRepo.findByPlanIdAndParentFolderIsNull(planId);

        return rootFolders.stream()
                .map(this::convertToNode)
                .collect(Collectors.toList());
    }

    private FolderNodeDTO convertToNode(Folder folder) {
        return FolderNodeDTO.builder()
                .id(folder.getId())
                .name(folder.getName())
                .description(folder.getDescription())
                .contents(folder.getContents().stream()
                        .map(c -> FolderContentDTO.builder()
                                .id(c.getId())
                                .name(c.getName())
                                .description(c.getDescription())
                                .required(c.isRequired())
                                .qrCode(c.getQrCode())
                                .build())
                        .collect(Collectors.toList()))
                // Appel récursif pour les sous-dossiers
                .children(folder.getSubFolders().stream()
                        .map(this::convertToNode)
                        .collect(Collectors.toList()))
                .build();
    }
}
