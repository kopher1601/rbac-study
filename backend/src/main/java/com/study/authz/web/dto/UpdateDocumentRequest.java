package com.study.authz.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateDocumentRequest(@NotBlank String title, String content) {
}
