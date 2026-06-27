package com.study.authz.web.dto;

import com.study.authz.domain.Folder;

public record FolderDto(
        Long id,
        String name,
        Long parentFolderId,
        Long ownerId,
        int sensitivityLevel) {

    /** 트랜잭션 안에서 호출해야 한다(parentFolder 가 LAZY). */
    public static FolderDto from(Folder folder) {
        Folder parent = folder.getParentFolder();
        return new FolderDto(
                folder.getId(),
                folder.getName(),
                parent == null ? null : parent.getId(),
                folder.getOwnerId(),
                folder.getSensitivityLevel());
    }
}
