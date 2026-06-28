package com.study.authz.web;

import com.study.authz.auth.CurrentUserService;
import com.study.authz.rbac.AccessDecisionExplainer;
import com.study.authz.rbac.RbacDocumentService;
import com.study.authz.security.AppUserDetails;
import com.study.authz.web.dto.CreateDocumentRequest;
import com.study.authz.web.dto.DecisionDto;
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
 * RBAC 문서 인가 데모. 각 메서드의 {@code @PreAuthorize} 가 핵심 학습 포인트다.
 *
 * <p>두 가지 검사 방식을 일부러 섞어 보여준다:
 * <ul>
 *   <li>목록 = {@code hasRole('VIEWER')} — 역할 기반(역할 계층으로 EDITOR/ADMIN 도 통과)
 *   <li>단건/생성/수정/삭제/공유 = {@code hasAuthority('document:...')} — 세분 권한 기반
 * </ul>
 */
@RestController
@RequestMapping("/api/rbac/documents")
public class RbacDocumentController {

    private final RbacDocumentService documentService;
    private final AccessDecisionExplainer decisionExplainer;
    private final CurrentUserService currentUserService;

    public RbacDocumentController(
            RbacDocumentService documentService,
            AccessDecisionExplainer decisionExplainer,
            CurrentUserService currentUserService) {
        this.documentService = documentService;
        this.decisionExplainer = decisionExplainer;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @PreAuthorize("hasRole('VIEWER')")
    public List<DocumentDto> list() {
        return documentService.list();
    }

    /** 현재 유저가 각 액션을 할 수 있는지 + 사유(인가 우회 없이 설명만). {@code /{id}} 보다 먼저 매칭된다. */
    @GetMapping("/decisions")
    public List<DecisionDto> decisions() {
        AppUserDetails user = currentUserService.currentUser().orElseThrow();
        return decisionExplainer.explainDocumentActions(user);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('document:read')")
    public DocumentDto get(@PathVariable Long id) {
        return documentService.get(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('document:write')")
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentDto create(@Valid @RequestBody CreateDocumentRequest request) {
        Long ownerId = currentUserService.currentUser().orElseThrow().getUserId();
        return documentService.create(request, ownerId);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('document:write')")
    public DocumentDto update(@PathVariable Long id, @Valid @RequestBody UpdateDocumentRequest request) {
        return documentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('document:delete')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        documentService.delete(id);
    }

    @PostMapping("/{id}/share")
    @PreAuthorize("hasAuthority('document:share')")
    public ShareResult share(@PathVariable Long id, @Valid @RequestBody ShareRequest request) {
        return documentService.share(id, request);
    }
}
