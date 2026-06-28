package com.study.authz.abac;

import com.study.authz.abac.model.Decision;
import com.study.authz.domain.Folder;
import com.study.authz.domain.repository.FolderRepository;
import com.study.authz.web.dto.AbacDecisionDto;
import com.study.authz.web.dto.AbacResourceDecisionsDto;
import com.study.authz.web.dto.FolderDto;
import com.study.authz.web.dto.UpdateFolderRequest;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * ABAC 폴더 도메인 로직. RBAC 폴더와 동일하게 액션은 읽기/쓰기 두 가지다.
 * 인가는 컨트롤러의 {@code @PreAuthorize("@abacAccess.can*Folder(#id)")} 가 처리한다.
 */
@Service
public class AbacFolderService {

    private final FolderRepository folderRepository;
    private final AbacRequestFactory requestFactory;
    private final PolicyDecisionPoint pdp;

    public AbacFolderService(
            FolderRepository folderRepository,
            AbacRequestFactory requestFactory,
            PolicyDecisionPoint pdp) {
        this.folderRepository = folderRepository;
        this.requestFactory = requestFactory;
        this.pdp = pdp;
    }

    @Transactional(readOnly = true)
    public List<FolderDto> listReadable() {
        return folderRepository.findAll().stream()
                .filter(folder -> pdp.decide(requestFactory.forFolder(folder, AbacAction.READ)).permitted())
                .map(FolderDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FolderDto get(Long id) {
        return FolderDto.from(find(id));
    }

    @Transactional
    public FolderDto update(Long id, UpdateFolderRequest request) {
        Folder folder = find(id);
        folder.rename(request.name());
        return FolderDto.from(folder);
    }

    @Transactional(readOnly = true)
    public List<AbacResourceDecisionsDto> explainAll() {
        return folderRepository.findAll().stream().map(this::explainFolder).toList();
    }

    @Transactional(readOnly = true)
    public AbacResourceDecisionsDto explain(Long id) {
        return explainFolder(find(id));
    }

    private AbacResourceDecisionsDto explainFolder(Folder folder) {
        List<AbacDecisionDto> decisions = Stream.of(AbacAction.READ, AbacAction.WRITE)
                .map(action -> {
                    Decision decision = pdp.decide(requestFactory.forFolder(folder, action));
                    return AbacDecisionDto.from(action, decision);
                })
                .toList();
        return new AbacResourceDecisionsDto(folder.getId(), folder.getName(), decisions);
    }

    private Folder find(Long id) {
        return folderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "folder not found: " + id));
    }
}
