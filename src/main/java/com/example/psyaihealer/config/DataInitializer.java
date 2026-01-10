package com.example.psyaihealer.config;

import com.example.psyaihealer.user.Role;
import com.example.psyaihealer.user.User;
import com.example.psyaihealer.user.UserRepository;
import com.example.psyaihealer.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Arrays;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initAdmin(UserService userService,
                                @Value("${app.bootstrap.admin.enabled:true}") boolean bootstrapEnabled,
                                @Value("${app.bootstrap.admin.username:admin}") String username,
                                @Value("${app.bootstrap.admin.password:}") String configuredPassword,
                                @Value("${app.bootstrap.admin.full-name:管理员}") String fullName,
                                @Value("${app.bootstrap.admin.email:admin@example.com}") String email,
                                @Value("${app.bootstrap.admin.reset-password:false}") boolean resetPassword,
                                Environment environment,
                                PasswordEncoder passwordEncoder,
                                UserRepository userRepository) {
        return args -> {
            if (!bootstrapEnabled) {
                log.info("已关闭默认管理员自举创建：app.bootstrap.admin.enabled=false");
                return;
            }
            initAdminUser(userService, userRepository, username, configuredPassword, fullName, email, resetPassword, passwordEncoder, environment);
        };
    }

    @Transactional
    void initAdminUser(UserService userService,
                       UserRepository userRepository,
                       String username,
                       String configuredPassword,
                       String fullName,
                       String email,
                       boolean resetPassword,
                       PasswordEncoder passwordEncoder,
                       Environment environment) {

        // 以“是否存在任意 ADMIN”作为判断条件，避免管理员用户名不是固定值时误创建。
        if (!userRepository.existsByRole(Role.ADMIN)) {
            String effectivePassword = configuredPassword;
            boolean missingPassword = (effectivePassword == null || effectivePassword.isBlank());
            boolean isProd = Arrays.stream(environment.getActiveProfiles())
                    .anyMatch(p -> p.equalsIgnoreCase("prod") || p.equalsIgnoreCase("production"));

            if (missingPassword) {
                if (isProd) {
                    throw new IllegalStateException("未配置默认管理员密码：请设置 app.bootstrap.admin.password");
                }
                effectivePassword = generateTempPassword();
                log.warn("未配置默认管理员密码（非生产环境），已生成临时密码用于启动：username={}，password={}。请尽快修改/通过环境变量配置固定密码。",
                        username,
                        effectivePassword);
            }

            if (userRepository.existsByUsername(username)) {
                // 用户名被占用但系统里又没有任何管理员，这会导致无法自举。
                throw new IllegalStateException("无法创建默认管理员：用户名已存在但系统中不存在任何 ADMIN 账号，请更换 app.bootstrap.admin.username 或手动修复数据");
            }

            User admin = userService.registerUser(username, effectivePassword, fullName, email, Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            if (missingPassword) {
                log.warn("已创建默认管理员账号：username={}。密码为临时生成值（已在上一条日志中输出），请尽快修改/改为固定配置。", username);
            } else {
                log.warn("已创建默认管理员账号：username={}，password=（已按配置设置）。请尽快修改默认密码。", username);
            }
            return;
        }

        // 可选：仅当明确开启时，才重置指定用户名的管理员密码。
        if (resetPassword && username != null && !username.isBlank()) {
            userRepository.findByUsername(username).ifPresent(existing -> {
                if (existing.getRole() != Role.ADMIN) {
                    log.warn("已跳过管理员密码重置：{} 的 role 不是 ADMIN。", username);
                    return;
                }
                if (configuredPassword == null || configuredPassword.isBlank()) {
                    log.warn("已跳过管理员密码重置：未配置 app.bootstrap.admin.password");
                    return;
                }
                existing.setPassword(passwordEncoder.encode(configuredPassword));
                existing.setEnabled(true);
                userRepository.save(existing);
                log.warn("已根据配置重置管理员密码并启用账号：username={}", username);
            });
        }
    }

    private static String generateTempPassword() {
        // 16 bytes -> 22 chars base64url-ish; we add a prefix to make it human-recognizable.
        byte[] raw = new byte[16];
        new SecureRandom().nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        return "admin-" + token;
    }
}
