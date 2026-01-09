package com.test.repository;

import com.test.model.FolderContent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FolderContentRepo extends JpaRepository<FolderContent, Long> {
    @Query("SELECT fc FROM FolderContent fc JOIN fc.folders f WHERE f.id = :folderId AND fc.required = true")
    List<FolderContent> findRequiredContentsByFolderId(@Param("folderId") Long folderId);

    boolean existsByName(String name);
}
