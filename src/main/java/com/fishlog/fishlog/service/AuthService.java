package com.fishlog.fishlog.service;

import com.fishlog.fishlog.domain.User;
import com.fishlog.fishlog.domain.enums.UserRole;
import com.fishlog.fishlog.dto.request.LoginRequest;
import com.fishlog.fishlog.dto.request.SignupRequest;
import com.fishlog.fishlog.dto.response.AuthResponse;
import com.fishlog.fishlog.repository.UserRepository;
import com.fishlog.fishlog.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("이미 사용 중인 사용자명입니다: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.ROLE_USER)
                .build();

        User saved = userRepository.save(user);
        String token = jwtTokenProvider.createToken(saved.getId());
        return AuthResponse.of(token, saved.getId(), saved.getUsername());
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("사용자명 또는 비밀번호가 올바르지 않습니다"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("사용자명 또는 비밀번호가 올바르지 않습니다");
        }

        String token = jwtTokenProvider.createToken(user.getId());
        return AuthResponse.of(token, user.getId(), user.getUsername());
    }
}
