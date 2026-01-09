package com.test.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.test.enums.FileType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "pieces")
@Entity
public class Piece extends BaseEntity {

    @NotBlank
    @Column(nullable = false, unique = true)
    private String fileName;

    @NotBlank
    @Column(nullable = false)
    private String filePath;

    private String pieceUrl;

    @Column(nullable = false)
    private Long fileSize; // Taille en bytes

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType fileType;

    @NotBlank
    @Column(nullable = false)
    private String qrCodeData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    @JsonIgnoreProperties("pieces")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_id", nullable = false)
    @JsonIgnoreProperties("pieces")
    private FolderContent content;
}
