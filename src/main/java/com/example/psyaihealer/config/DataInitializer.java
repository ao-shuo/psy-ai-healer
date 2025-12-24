package com.example.psyaihealer.config;

import com.example.psyaihealer.user.Role;
import com.example.psyaihealer.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initAdmin(UserService userService) {
        return args -> {
            if (!userService.getRepository().existsByUsername("admin")) {
                userService.registerUser("admin", "admin123", "管理员", "admin@example.com", Set.of(Role.ADMIN));
            }
        };
    }
}
