package com.study.authz.web;

import com.study.authz.abac.AbacFolderService;
import com.study.authz.web.dto.AbacResourceDecisionsDto;
import com.study.authz.web.dto.FolderDto;
import com.study.authz.web.dto.UpdateFolderRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ABAC 폴더 인가 데모. 폴더 계층도 같은 정책(소유/등급/부서/업무시간)으로 per-resource 인가된다.
 * RBAC 폴더와 동일하게 액션은 읽기/쓰기 두 가지.
 */
@RestController
@RequestMapping("/api/abac/folders")
public class AbacFolderController {

    private final AbacFolderService folderService;

    public AbacFolderController(AbacFolderService folderService) {
        this.folderService = folderService;
    }

    @GetMapping
    public List<FolderDto> list() {
        return folderService.listReadable();
    }

    @GetMapping("/decisions")
    public List<AbacResourceDecisionsDto> decisions() {
        return folderService.explainAll();
    }

    @GetMapping("/{id}/decisions")
    public AbacResourceDecisionsDto decision(@PathVariable Long id) {
        return folderService.explain(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@abacAccess.canReadFolder(#id)")
    public FolderDto get(@PathVariable Long id) {
        return folderService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@abacAccess.canWriteFolder(#id)")
    public FolderDto update(@PathVariable Long id, @Valid @RequestBody UpdateFolderRequest request) {
        return folderService.update(id, request);
    }
}
