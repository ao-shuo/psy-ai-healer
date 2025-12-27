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
import org.springframework.security.crypto.password.PasswordEncoder;
import java.security.SecureRandom;
import java.util.Arrays;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initAdmin(UserService userService,
                                @Value("${app.bootstrap.admin.enabled:true}") boolean bootstrapEnabled,
                                @Value("${app.bootstrap.admin.username:admin}") String username,
                                @Value("${app.bootstrap.admin.password:}") String configuredPassword,
                                PasswordEncoder passwordEncoder,
                                Environment environment) {
        return args -> {
            if (!bootstrapEnabled) {
                log.info("已关闭默认管理员自举创建：app.bootstrap.admin.enabled=false");
                return;
            }
            boolean isProd = Arrays.stream(environment.getActiveProfiles())
                    .anyMatch(p -> p.equalsIgnoreCase("prod") || p.equalsIgnoreCase("production"));
            if (!userService.getRepository().existsByUsername(username)) {
                boolean missingPassword = (configuredPassword == null || configuredPassword.isBlank()
                        || "auto-generate".equalsIgnoreCase(configuredPassword));
                if (isProd && missingPassword) {
                    throw new IllegalStateException("生产环境必须通过 app.bootstrap.admin.password（或环境变量 APP_BOOTSTRAP_ADMIN_PASSWORD）设置管理员密码");
                }
                String password = missingPassword
                        ? generateSecurePassword()
                        : configuredPassword;
                var admin = userService.registerUser(username, password, "管理员", "admin@example.com", Role.ADMIN);
                admin.setEnabled(true);
                userService.getRepository().save(admin);
                if (missingPassword) {
                    String message = "默认管理员创建完成，用户名=%s，临时密码=%s，请设置 app.bootstrap.admin.password（或环境变量 APP_BOOTSTRAP_ADMIN_PASSWORD）后重启。"
                            .formatted(username, password);
                    if (isProd) {
                        log.warn("生产环境不应出现自动生成的管理员密码，请检查配置。");
                    } else {
                        log.warn(message);
                    }
                } else {
                    log.info("默认管理员创建完成，用户名={}。", username);
                }
            } else {
                // 兼容历史数据：如果管理员已存在但被禁用，自动启用，确保系统可管理
                userService.getRepository().findByUsername(username).ifPresent(existing -> {
                    boolean changed = false;
                    if (!existing.isEnabled()) {
                        existing.setEnabled(true);
                        changed = true;
                        log.warn("检测到默认管理员账号被禁用，已自动启用。用户名={}", username);
                    }

                    boolean providedPassword = configuredPassword != null
                            && !configuredPassword.isBlank()
                            && !"auto-generate".equalsIgnoreCase(configuredPassword);
                    if (providedPassword) {
                        existing.setPassword(passwordEncoder.encode(configuredPassword));
                        changed = true;
                        log.warn("已根据配置重置默认管理员密码（app.bootstrap.admin.password）。用户名={}", username);
                    }

                    if (changed) {
                        userService.getRepository().save(existing);
                    }
                });
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
