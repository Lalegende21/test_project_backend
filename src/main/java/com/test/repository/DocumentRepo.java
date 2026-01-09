package com.test.repository;

import com.test.enums.DocumentStatus;
import com.test.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepo extends JpaRepository<Document, Long> {
    List<Document> findByTitleContainingIgnoreCaseAndStatus(String title, DocumentStatus status);

    List<Document> findByTitleContainingIgnoreCase(String title);

    List<Document> findByStatus(DocumentStatus status);

    long countByStatus(DocumentStatus documentStatus);
}
