package com.test.repository;

import com.test.model.DocumentUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentUserRepo extends JpaRepository<DocumentUser, Long> {
    Optional<DocumentUser> findByDocumentIdAndUserId(Long documentId, Long userId);
    List<DocumentUser> findByDocumentId(Long documentId);
    List<DocumentUser> findByUserId(Long userId);
    boolean existsByDocumentIdAndUserId(Long documentId, Long userId);

    @Query("SELECT du FROM DocumentUser du WHERE du.document.id = :documentId AND du.user.username = :username")
    Optional<DocumentUser> findByDocumentIdAndUsername(@Param("documentId") Long documentId, @Param("username") String username);
}
