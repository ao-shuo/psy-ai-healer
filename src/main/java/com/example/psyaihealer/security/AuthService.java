package com.example.psyaihealer.security;

import com.example.psyaihealer.config.RegistrationProperties;
import com.example.psyaihealer.dto.AuthRequest;
import com.example.psyaihealer.dto.AuthResponse;
import com.example.psyaihealer.dto.RegisterRequest;
import com.example.psyaihealer.user.Role;
import com.example.psyaihealer.user.User;
import com.example.psyaihealer.user.UserRepository;
import com.example.psyaihealer.user.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final RegistrationProperties registrationProperties;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserService userService,
                       UserRepository userRepository,
                       RegistrationProperties registrationProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.registrationProperties = registrationProperties;
    }

    public AuthResponse register(RegisterRequest request) {
        Role desiredRole = resolveRole(request.getRole());
        validateRegistrationCode(desiredRole, request.getRegistrationCode());
        User user = userService.registerUser(
                request.getUsername(),
                request.getPassword(),
                request.getFullName(),
                request.getEmail(),
                Set.of(desiredRole));

        String token = jwtService.generateToken(user.getUsername(), Map.of("roles", user.getRoles()));
        return new AuthResponse(token, user.getUsername(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        String token = jwtService.generateToken(request.getUsername(), Map.of("roles", roles));
        return new AuthResponse(token, request.getUsername(), roles);
    }

    public User currentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
    }

    private Role resolveRole(String candidate) {
        if (candidate == null) {
            return Role.USER;
        }
        try {
            return Role.valueOf(candidate.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return Role.USER;
        }
    }

    private void validateRegistrationCode(Role role, String providedCode) {
        if (role == Role.USER) {
            return;
        }
        String expected;
        if (role == Role.ADMIN) {
            expected = registrationProperties.getAdminCode();
        } else if (role == Role.THERAPIST) {
            expected = registrationProperties.getTherapistCode();
        } else {
            throw new IllegalArgumentException("角色不被允许");
        }
        if (expected == null || expected.isBlank()) {
            throw new IllegalArgumentException("角色" + role + "的注册已关闭");
        }
        if (!expected.equals(providedCode)) {
            throw new IllegalArgumentException("注册码错误");
        }
    }
}
