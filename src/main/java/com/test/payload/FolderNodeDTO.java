package com.test.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FolderNodeDTO {
    private Long id;
    private String name;
    private String description;
    private List<FolderNodeDTO> children;
    private List<FolderContentDTO> contents;
}
