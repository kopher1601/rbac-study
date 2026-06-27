package com.study.authz.web.dto;

import com.study.authz.domain.Document;
import com.study.authz.domain.Folder;

public record DocumentDto(
        Long id,
        String title,
        String content,
        Long folderId,
        String folderName,
        Long ownerId,
        int sensitivityLevel) {

    /** 트랜잭션 안에서 호출해야 한다(folder 가 LAZY). */
    public static DocumentDto from(Document doc) {
        Folder folder = doc.getFolder();
        return new DocumentDto(
                doc.getId(),
                doc.getTitle(),
                doc.getContent(),
                folder == null ? null : folder.getId(),
                folder == null ? null : folder.getName(),
                doc.getOwnerId(),
                doc.getSensitivityLevel());
    }
}
