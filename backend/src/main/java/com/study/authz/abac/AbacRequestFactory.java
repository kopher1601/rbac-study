package com.study.authz.abac;

import com.study.authz.abac.env.EnvironmentAttributeProvider;
import com.study.authz.abac.model.AccessRequest;
import com.study.authz.abac.model.Environment;
import com.study.authz.abac.model.Resource;
import com.study.authz.abac.model.Subject;
import com.study.authz.auth.CurrentUserService;
import com.study.authz.domain.Document;
import com.study.authz.domain.Folder;
import com.study.authz.domain.User;
import com.study.authz.domain.repository.DocumentRepository;
import com.study.authz.domain.repository.FolderRepository;
import com.study.authz.domain.repository.UserRepository;
import com.study.authz.security.AppUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * PDP 입력({@link AccessRequest})을 조립하는 단일 지점 — 주체·리소스·환경 속성을 한곳에서 모은다.
 *
 * <ul>
 *   <li><b>주체</b>: {@code AppUserDetails} 스냅샷에서 추출(LAZY 없음).
 *   <li><b>리소스</b>: 문서/폴더 엔티티 + {@code ownerDepartment}(소유자의 부서)를 파생.
 *       부서 파생을 위해 owner 를 조회하므로 {@code @Transactional} 경계 안에서 동작한다.
 *   <li><b>환경</b>: {@code EnvironmentAttributeProvider.now()} 로부터 업무시간을 파생({@code Environment.at}).
 * </ul>
 *
 * <p>id 기반 메서드는 {@code @PreAuthorize} 평가 시점(컨트롤러 진입 전)에 호출되므로 직접 트랜잭션을 연다.
 * 엔티티 기반 오버로드는 이미 로딩된 엔티티를 받아 목록 필터링 시 N+1 재조회를 피한다(호출자 트랜잭션 사용).
 */
@Component
public class AbacRequestFactory {

    private final CurrentUserService currentUserService;
    private final DocumentRepository documentRepository;
    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final EnvironmentAttributeProvider environmentProvider;

    public AbacRequestFactory(
            CurrentUserService currentUserService,
            DocumentRepository documentRepository,
            FolderRepository folderRepository,
            UserRepository userRepository,
            EnvironmentAttributeProvider environmentProvider) {
        this.currentUserService = currentUserService;
        this.documentRepository = documentRepository;
        this.folderRepository = folderRepository;
        this.userRepository = userRepository;
        this.environmentProvider = environmentProvider;
    }

    @Transactional(readOnly = true)
    public AccessRequest forDocument(Long documentId, AbacAction action) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> notFound("document", documentId));
        return forDocument(document, action);
    }

    /** 이미 로딩된 문서로 요청 조립(목록 필터링·설명에서 재사용). 호출자의 트랜잭션 안에서 호출한다. */
    public AccessRequest forDocument(Document document, AbacAction action) {
        Resource resource = new Resource(
                "document",
                document.getId(),
                document.getOwnerId(),
                document.getSensitivityLevel(),
                departmentOf(document.getOwnerId()));
        return assemble(resource, action);
    }

    @Transactional(readOnly = true)
    public AccessRequest forFolder(Long folderId, AbacAction action) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> notFound("folder", folderId));
        return forFolder(folder, action);
    }

    /** 이미 로딩된 폴더로 요청 조립. 호출자의 트랜잭션 안에서 호출한다. */
    public AccessRequest forFolder(Folder folder, AbacAction action) {
        Resource resource = new Resource(
                "folder",
                folder.getId(),
                folder.getOwnerId(),
                folder.getSensitivityLevel(),
                departmentOf(folder.getOwnerId()));
        return assemble(resource, action);
    }

    private AccessRequest assemble(Resource resource, AbacAction action) {
        AppUserDetails user = currentUserService.currentUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        Subject subject = new Subject(
                user.getUserId(),
                user.getUsername(),
                user.getDepartment(),
                user.getClearanceLevel(),
                user.getRoleNames());
        Environment environment = Environment.at(environmentProvider.now());
        return new AccessRequest(subject, resource, action, environment);
    }

    /** 리소스 소유 부서 = 소유자의 부서(문서/폴더에는 부서 컬럼이 없으므로 파생). */
    private String departmentOf(Long ownerId) {
        return userRepository.findById(ownerId).map(User::getDepartment).orElse(null);
    }

    private ResponseStatusException notFound(String what, Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, what + " not found: " + id);
    }
}
