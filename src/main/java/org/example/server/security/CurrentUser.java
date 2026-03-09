package org.example.server.security;

import lombok.RequiredArgsConstructor;
import org.example.server.user.ApplicationUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final ApplicationUserRepository userRepository;

    public Long id(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("user not found"))
                .getId();
    }
}