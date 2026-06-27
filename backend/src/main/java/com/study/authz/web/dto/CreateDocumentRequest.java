package com.study.authz.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDocumentRequest(
        @NotBlank String title,
        String content,
        Long folderId,
        int sensitivityLevel) {
}
