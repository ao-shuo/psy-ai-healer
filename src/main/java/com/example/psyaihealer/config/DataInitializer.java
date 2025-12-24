package com.example.psyaihealer.config;

import com.example.psyaihealer.user.Role;
import com.example.psyaihealer.user.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.UUID;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initAdmin(UserService userService,
                                @Value("${app.bootstrap.admin.username:admin}") String username,
                                @Value("${app.bootstrap.admin.password:}") String configuredPassword) {
        return args -> {
            if (!userService.getRepository().existsByUsername(username)) {
                String password = (configuredPassword == null || configuredPassword.isBlank())
                        ? UUID.randomUUID().toString()
                        : configuredPassword;
                userService.registerUser(username, password, "管理员", "admin@example.com", Set.of(Role.ADMIN));
                log.warn("默认管理员创建完成，用户名={}，临时密码={}，请在配置中显式设置并尽快修改。", username, password);
            }
        };
    }
}
