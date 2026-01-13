package com.test.enums;

import java.util.HashSet;
import java.util.Set;

public enum DocumentRole {
    OWNER(Set.of(
            DocumentPermission.VIEW,
            DocumentPermission.EDIT,
            DocumentPermission.UPLOAD,
            DocumentPermission.DELETE,
            DocumentPermission.VALIDATE,
            DocumentPermission.MANAGE
    )),

    EDITOR(Set.of(
            DocumentPermission.VIEW,
            DocumentPermission.EDIT,
            DocumentPermission.UPLOAD,
            DocumentPermission.DELETE
    )),

    CONTRIBUTOR(Set.of(
            DocumentPermission.VIEW,
            DocumentPermission.UPLOAD
    )),

    VALIDATOR(Set.of(
            DocumentPermission.VIEW,
            DocumentPermission.VALIDATE
    )),

    VIEWER(Set.of(
            DocumentPermission.VIEW
    ));

    private final Set<DocumentPermission> permissions;

    DocumentRole(Set<DocumentPermission> permissions) {
        this.permissions = permissions;
    }

    public Set<DocumentPermission> getPermissions() {
        return new HashSet<>(permissions);
    }
}
