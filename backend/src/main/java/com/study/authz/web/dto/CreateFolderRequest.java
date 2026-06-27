package com.study.authz.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateFolderRequest(
        @NotBlank String name,
        Long parentFolderId,
        int sensitivityLevel) {
}
