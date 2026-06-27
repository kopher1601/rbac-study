package com.study.authz.rbac;

import com.study.authz.domain.Folder;
import com.study.authz.domain.repository.FolderRepository;
import com.study.authz.web.dto.CreateFolderRequest;
import com.study.authz.web.dto.FolderDto;
import com.study.authz.web.dto.UpdateFolderRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RbacFolderService {

    private final FolderRepository folderRepository;

    public RbacFolderService(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    @Transactional(readOnly = true)
    public List<FolderDto> list() {
        return folderRepository.findAll().stream().map(FolderDto::from).toList();
    }

    @Transactional(readOnly = true)
    public FolderDto get(Long id) {
        return FolderDto.from(find(id));
    }

    @Transactional
    public FolderDto create(CreateFolderRequest request, Long ownerId) {
        Folder parent = resolveParent(request.parentFolderId());
        Folder folder = new Folder(request.name(), parent, ownerId, request.sensitivityLevel());
        return FolderDto.from(folderRepository.save(folder));
    }

    @Transactional
    public FolderDto update(Long id, UpdateFolderRequest request) {
        Folder folder = find(id);
        folder.rename(request.name());
        return FolderDto.from(folder);
    }

    private Folder resolveParent(Long parentId) {
        if (parentId == null) {
            return null;
        }
        return folderRepository.findById(parentId).orElseThrow(() -> notFound(parentId));
    }

    private Folder find(Long id) {
        return folderRepository.findById(id).orElseThrow(() -> notFound(id));
    }

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "folder not found: " + id);
    }
}
