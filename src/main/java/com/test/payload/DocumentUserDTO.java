package com.test.payload;

import com.test.enums.DocumentPermission;
import com.test.enums.DocumentRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DocumentUserDTO {

    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private DocumentRole role;
    private Set<DocumentPermission> permissions;
    private LocalDateTime assignedAt;
    private String assignedByUsername;
}
