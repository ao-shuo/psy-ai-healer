package com.example.psyaihealer.config;

import com.example.psyaihealer.user.Role;
import com.example.psyaihealer.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.security.SecureRandom;
import java.util.Arrays;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initAdmin(UserService userService,
                                @Value("${app.bootstrap.admin.username:admin}") String username,
                                @Value("${app.bootstrap.admin.password:}") String configuredPassword,
                                Environment environment) {
        return args -> {
            boolean isProd = Arrays.stream(environment.getActiveProfiles())
                    .anyMatch(p -> p.equalsIgnoreCase("prod") || p.equalsIgnoreCase("production"));
            if (!userService.getRepository().existsByUsername(username)) {
                boolean missingPassword = (configuredPassword == null || configuredPassword.isBlank()
                        || "auto-generate".equalsIgnoreCase(configuredPassword));
                if (isProd && missingPassword) {
                    throw new IllegalStateException("生产环境必须通过 APP_ADMIN_PASSWORD 设置管理员密码");
                }
                String password = missingPassword
                        ? generateSecurePassword()
                        : configuredPassword;
                userService.registerUser(username, password, "管理员", "admin@example.com", Set.of(Role.ADMIN));
                if (missingPassword) {
                    log.warn("默认管理员创建完成，用户名={}，已为本次运行生成临时密码，请设置 APP_ADMIN_PASSWORD 后重启。", username);
                } else {
                    log.info("默认管理员创建完成，用户名={}。", username);
                }
            }
        };
    }

    private String generateSecurePassword() {
        String chars = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 24; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
