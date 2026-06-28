package com.study.authz.abac;

import com.study.authz.abac.model.Decision;
import com.study.authz.domain.Document;
import com.study.authz.domain.repository.DocumentRepository;
import com.study.authz.web.dto.AbacDecisionDto;
import com.study.authz.web.dto.AbacResourceDecisionsDto;
import com.study.authz.web.dto.DocumentDto;
import com.study.authz.web.dto.ShareRequest;
import com.study.authz.web.dto.ShareResult;
import com.study.authz.web.dto.UpdateDocumentRequest;
import java.util.Arrays;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * ABAC 문서 도메인 로직. {@code get/update/delete/share} 의 인가는 컨트롤러의
 * {@code @PreAuthorize("@abacAccess...")} 가 이미 통과시킨 뒤이므로 여기서는 순수 CRUD 만 한다(RBAC 패턴).
 *
 * <p>{@code listReadable()} 은 RBAC 와의 대조 포인트: RBAC 목록은 역할만 맞으면 전부 반환했지만,
 * ABAC 는 <b>행(문서) 단위로 PDP READ 결정</b>을 적용해 읽을 수 있는 것만 남긴다.
 */
@Service
public class AbacDocumentService {

    private final DocumentRepository documentRepository;
    private final AbacRequestFactory requestFactory;
    private final PolicyDecisionPoint pdp;

    public AbacDocumentService(
            DocumentRepository documentRepository,
            AbacRequestFactory requestFactory,
            PolicyDecisionPoint pdp) {
        this.documentRepository = documentRepository;
        this.requestFactory = requestFactory;
        this.pdp = pdp;
    }

    @Transactional(readOnly = true)
    public List<DocumentDto> listReadable() {
        return documentRepository.findAll().stream()
                .filter(doc -> pdp.decide(requestFactory.forDocument(doc, AbacAction.READ)).permitted())
                .map(DocumentDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentDto get(Long id) {
        return DocumentDto.from(find(id));
    }

    @Transactional
    public DocumentDto update(Long id, UpdateDocumentRequest request) {
        Document document = find(id);
        document.update(request.title(), request.content());
        return DocumentDto.from(document); // dirty checking 으로 커밋 시 반영
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
                "ABAC: 정책(소유/등급/부서/업무시간) 평가를 통과해 공유가 인가되었습니다. "
                        + "실제 공유 관계(RelationTuple) 저장은 Stage 3(ReBAC)에서 구현됩니다.");
    }

    /** 모든 문서 × 모든 액션의 결정 + 규칙 trace(설명 엔드포인트). 인가를 우회하지 않고 PDP 호출만 한다. */
    @Transactional(readOnly = true)
    public List<AbacResourceDecisionsDto> explainAll() {
        return documentRepository.findAll().stream().map(this::explainDocument).toList();
    }

    @Transactional(readOnly = true)
    public AbacResourceDecisionsDto explain(Long id) {
        return explainDocument(find(id));
    }

    private AbacResourceDecisionsDto explainDocument(Document document) {
        List<AbacDecisionDto> decisions = Arrays.stream(AbacAction.values())
                .map(action -> {
                    Decision decision = pdp.decide(requestFactory.forDocument(document, action));
                    return AbacDecisionDto.from(action, decision);
                })
                .toList();
        return new AbacResourceDecisionsDto(document.getId(), document.getTitle(), decisions);
    }

    private Document find(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "document not found: " + id));
    }
}
