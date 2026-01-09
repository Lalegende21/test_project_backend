package com.test.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DocumentUpdateDTO {
    private String title;
    private String description;
    private Map<String, Object> metadata;
}
