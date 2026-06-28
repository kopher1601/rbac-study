package com.study.authz.auth;

import com.study.authz.security.AppUserDetails;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * 현재 SecurityContext 에서 인증 주체({@link AppUserDetails})를 꺼낸다.
 * 미인증/익명(AnonymousAuthenticationToken)일 때는 비어 있는 Optional 을 돌려준다.
 */
@Service
public class CurrentUserService {

    public Optional<AppUserDetails> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof AppUserDetails details) {
            return Optional.of(details);
        }
        return Optional.empty();
    }
}
