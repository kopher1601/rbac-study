package com.study.authz.web;

import com.study.authz.abac.AbacDocumentService;
import com.study.authz.web.dto.AbacResourceDecisionsDto;
import com.study.authz.web.dto.DocumentDto;
import com.study.authz.web.dto.ShareRequest;
import com.study.authz.web.dto.ShareResult;
import com.study.authz.web.dto.UpdateDocumentRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * ABAC 문서 인가 데모. RBAC 컨트롤러와 <b>나란히 대조</b>되도록 같은 액션 집합을 갖되,
 * 인가는 정적 {@code hasAuthority(...)} 가 아니라 <b>리소스 속성을 보는</b>
 * {@code @PreAuthorize("@abacAccess.can*Document(#id)")} 로 한다.
 *
 * <ul>
 *   <li>목록 = PDP READ 결정으로 <b>행 단위 필터</b>(RBAC 가 전부 반환한 것과 대조)
 *   <li>단건/수정/삭제/공유 = 그 문서의 소유자·민감도·부서·시간으로 per-resource 결정
 *   <li>{@code /decisions} = 인가 우회 없이 결정+사유(trace)만 설명(거부 케이스도 200 으로 사유 노출)
 * </ul>
 */
@RestController
@RequestMapping("/api/abac/documents")
public class AbacDocumentController {

    private final AbacDocumentService documentService;

    public AbacDocumentController(AbacDocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<DocumentDto> list() {
        return documentService.listReadable();
    }

    /** 모든 문서 × 액션의 결정+규칙 trace. {@code /{id}} 보다 먼저 매칭된다. */
    @GetMapping("/decisions")
    public List<AbacResourceDecisionsDto> decisions() {
        return documentService.explainAll();
    }

    /** 한 문서의 액션별 결정+규칙 trace. */
    @GetMapping("/{id}/decisions")
    public AbacResourceDecisionsDto decision(@PathVariable Long id) {
        return documentService.explain(id);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@abacAccess.canReadDocument(#id)")
    public DocumentDto get(@PathVariable Long id) {
        return documentService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@abacAccess.canWriteDocument(#id)")
    public DocumentDto update(@PathVariable Long id, @Valid @RequestBody UpdateDocumentRequest request) {
        return documentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@abacAccess.canDeleteDocument(#id)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        documentService.delete(id);
    }

    @PostMapping("/{id}/share")
    @PreAuthorize("@abacAccess.canShareDocument(#id)")
    public ShareResult share(@PathVariable Long id, @Valid @RequestBody ShareRequest request) {
        return documentService.share(id, request);
    }
}
