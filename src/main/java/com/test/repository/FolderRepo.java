package com.test.repository;

import com.test.model.Folder;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FolderRepo extends JpaRepository<Folder, Long> {
    List<Folder> findByPlanIdAndParentFolderIsNull(Long planId);

    boolean existsByName(String name);
}
