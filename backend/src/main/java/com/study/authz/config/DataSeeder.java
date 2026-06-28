package com.study.authz.config;

import com.study.authz.domain.Document;
import com.study.authz.domain.Folder;
import com.study.authz.domain.Permission;
import com.study.authz.domain.Role;
import com.study.authz.domain.User;
import com.study.authz.domain.repository.DocumentRepository;
import com.study.authz.domain.repository.FolderRepository;
import com.study.authz.domain.repository.PermissionRepository;
import com.study.authz.domain.repository.RoleRepository;
import com.study.authz.domain.repository.UserRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 학습용 시드 데이터(유저/역할/권한/폴더/문서)를 생성한다.
 *
 * <p>{@code data.sql} 대신 자바 시더를 쓰는 이유: 다대다 조인(user_roles, role_permissions),
 * 자기참조(folder.parent), BCrypt 해시 같은 런타임 계산이 얽혀 있어 SQL 은 Hibernate 가 생성한
 * 스키마에 강결합되어 깨지기 쉽다. Repository 그래프로 작성하면 스키마 변경에 강하고 의도가 드러난다.
 *
 * <p>H2 인메모리 + {@code ddl-auto=create-drop} 이라 매 기동 시 다시 도므로 멱등 가드를 둔다.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /** 모든 시드 유저 공통 비밀번호(학습용). 프런트 로그인 화면에 안내한다. */
    public static final String COMMON_PASSWORD = "password";

    // 기본 권한 카탈로그
    private static final String DOC_READ = "document:read";
    private static final String DOC_WRITE = "document:write";
    private static final String DOC_DELETE = "document:delete";
    private static final String DOC_SHARE = "document:share";
    private static final String FOLDER_READ = "folder:read";
    private static final String FOLDER_WRITE = "folder:write";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final FolderRepository folderRepository;
    private final DocumentRepository documentRepository;
    private final PasswordEncoder passwordEncoder;

    /** 같은 권한 이름을 여러 역할이 공유하므로 한 번만 생성해 재사용한다. */
    private final Map<String, Permission> permissionCache = new LinkedHashMap<>();

    public DataSeeder(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            FolderRepository folderRepository,
            DocumentRepository documentRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.folderRepository = folderRepository;
        this.documentRepository = documentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("[DataSeeder] 시드 데이터가 이미 존재하여 건너뜁니다.");
            return;
        }
        log.info("[DataSeeder] 학습용 시드 데이터를 생성합니다.");

        // 1) 기본 역할 — 상위 역할은 하위 역할의 권한을 모두 펼쳐서 보유한다.
        //    (RoleHierarchy 빈은 hasRole 만 전이시키고 hasAuthority 는 전이시키지 않으므로)
        Role viewer = createRole("VIEWER", DOC_READ, FOLDER_READ);
        Role editor = createRole("EDITOR", DOC_READ, FOLDER_READ, DOC_WRITE, FOLDER_WRITE);
        Role admin = createRole(
                "ADMIN", DOC_READ, FOLDER_READ, DOC_WRITE, FOLDER_WRITE, DOC_DELETE, DOC_SHARE);

        // 2) 역할 폭발(Role Explosion) 데모용 부서별 스코프 역할.
        //    순수 RBAC 은 "특정 부서/폴더에서만 편집" 을 역할 하나로 표현하지 못해,
        //    (부서 × 액션) 마다 새 역할/스코프 권한이 필요해진다 → 역할 수가 폭증.
        createRole("ENGINEERING_FOLDER_EDITOR", "document:read@ENGINEERING", "document:write@ENGINEERING");
        Role legalEditor =
                createRole("LEGAL_FOLDER_EDITOR", "document:read@LEGAL", "document:write@LEGAL");
        createRole("HR_FOLDER_EDITOR", "document:read@HR", "document:write@HR");

        // 3) 유저(공통 비밀번호)
        User alice = createUser("alice", "ENGINEERING", 5, admin);
        User bob = createUser("bob", "ENGINEERING", 3, editor);
        User carol = createUser("carol", "LEGAL", 2, viewer);
        // dave: 전역 EDITOR 에 더해 부서 스코프 역할까지 — "한 사람에 역할 덕지덕지" 의 전형
        createUser("dave", "ENGINEERING", 3, editor, legalEditor);

        // 4) 폴더(계층) / 문서
        Folder engineering = folderRepository.save(new Folder("Engineering", null, alice.getId(), 3));
        Folder specs = folderRepository.save(new Folder("Specs", engineering, bob.getId(), 3));
        Folder legal = folderRepository.save(new Folder("Legal", null, carol.getId(), 4));

        documentRepository.save(new Document("API Design v2", "REST 인가 모델 비교 설계 문서", specs, bob.getId(), 3));
        documentRepository.save(new Document("Roadmap 2026", "분기별 제품 로드맵", engineering, alice.getId(), 2));
        documentRepository.save(new Document("NDA Template", "기밀 유지 계약 템플릿", legal, carol.getId(), 4));

        log.info(
                "[DataSeeder] 완료: users={}, roles={}, permissions={}, folders={}, documents={}",
                userRepository.count(),
                roleRepository.count(),
                permissionRepository.count(),
                folderRepository.count(),
                documentRepository.count());
    }

    private Role createRole(String name, String... permissionNames) {
        Role role = new Role(name);
        for (String permissionName : permissionNames) {
            role.addPermission(getOrCreatePermission(permissionName));
        }
        return roleRepository.save(role);
    }

    private Permission getOrCreatePermission(String name) {
        return permissionCache.computeIfAbsent(name, n -> permissionRepository.save(new Permission(n)));
    }

    private User createUser(String username, String department, int clearanceLevel, Role... roles) {
        User user = new User(username, passwordEncoder.encode(COMMON_PASSWORD), department, clearanceLevel);
        for (Role role : roles) {
            user.addRole(role);
        }
        return userRepository.save(user);
    }
}
