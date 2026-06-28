package com.study.authz.web;

import com.study.authz.auth.CurrentUserService;
import com.study.authz.rbac.RbacFolderService;
import com.study.authz.web.dto.CreateFolderRequest;
import com.study.authz.web.dto.FolderDto;
import com.study.authz.web.dto.UpdateFolderRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** RBAC 폴더 인가 데모. 읽기={@code folder:read}, 쓰기={@code folder:write}. */
@RestController
@RequestMapping("/api/rbac/folders")
public class RbacFolderController {

    private final RbacFolderService folderService;
    private final CurrentUserService currentUserService;

    public RbacFolderController(RbacFolderService folderService, CurrentUserService currentUserService) {
        this.folderService = folderService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('folder:read')")
    public List<FolderDto> list() {
        return folderService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('folder:read')")
    public FolderDto get(@PathVariable Long id) {
        return folderService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('folder:write')")
    @ResponseStatus(HttpStatus.CREATED)
    public FolderDto create(@Valid @RequestBody CreateFolderRequest request) {
        Long ownerId = currentUserService.currentUser().orElseThrow().getUserId();
        return folderService.create(request, ownerId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('folder:write')")
    public FolderDto update(@PathVariable Long id, @Valid @RequestBody UpdateFolderRequest request) {
        return folderService.update(id, request);
    }
}
