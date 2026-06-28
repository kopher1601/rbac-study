package com.study.authz.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateFolderRequest(@NotBlank String name) {
}
