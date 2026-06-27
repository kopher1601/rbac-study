package com.study.authz.rbac;

import com.study.authz.domain.Document;
import com.study.authz.domain.Folder;
import com.study.authz.domain.repository.DocumentRepository;
import com.study.authz.domain.repository.FolderRepository;
import com.study.authz.web.dto.CreateDocumentRequest;
import com.study.authz.web.dto.DocumentDto;
import com.study.authz.web.dto.ShareRequest;
import com.study.authz.web.dto.ShareResult;
import com.study.authz.web.dto.UpdateDocumentRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * 문서 CRUD/공유의 도메인 로직. 인가(@PreAuthorize)는 컨트롤러 레이어에서 처리하고,
 * 여기서는 비즈니스 동작만 수행한다. 매핑은 트랜잭션 안에서 DTO 로 끝낸다(LAZY 안전).
 */
@Service
public class RbacDocumentService {

    private final DocumentRepository documentRepository;
    private final FolderRepository folderRepository;

    public RbacDocumentService(DocumentRepository documentRepository, FolderRepository folderRepository) {
        this.documentRepository = documentRepository;
        this.folderRepository = folderRepository;
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> list() {
        return documentRepository.findAll().stream().map(DocumentDto::from).toList();
    }

    @Transactional(readOnly = true)
    public DocumentDto get(Long id) {
        return DocumentDto.from(find(id));
    }

    @Transactional
    public DocumentDto create(CreateDocumentRequest request, Long ownerId) {
        Folder folder = resolveFolder(request.folderId());
        Document document =
                new Document(request.title(), request.content(), folder, ownerId, request.sensitivityLevel());
        return DocumentDto.from(documentRepository.save(document));
    }

    @Transactional
    public DocumentDto update(Long id, UpdateDocumentRequest request) {
        Document document = find(id);
        document.update(request.title(), request.content());
        return DocumentDto.from(document); // 변경 감지(dirty checking)로 커밋 시 반영
    }

    @Transactional
    public void delete(Long id) {
        documentRepository.delete(find(id));
    }

    @Transactional(readOnly = true)
    public ShareResult share(Long id, ShareRequest request) {
        Document document = find(id);
        return new ShareResult(
                document.getId(),
                request.targetUsername(),
                "RBAC: 'document:share' 권한으로 공유가 인가되었습니다. "
                        + "실제 공유 관계(RelationTuple) 저장은 Stage 3(ReBAC)에서 구현됩니다.");
    }

    private Folder resolveFolder(Long folderId) {
        if (folderId == null) {
            return null;
        }
        return folderRepository.findById(folderId)
                .orElseThrow(() -> notFound("folder", folderId));
    }

    private Document find(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> notFound("document", id));
    }

    private ResponseStatusException notFound(String what, Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found: " + id);
    }
}
