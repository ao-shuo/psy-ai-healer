package com.example.psyaihealer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.registration")
public class RegistrationProperties {
    private String adminCode = "admin-2025-secret";
    private String counselorCode = "counselor-2025-secret";

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public String getCounselorCode() {
        return counselorCode;
    }

    public void setCounselorCode(String counselorCode) {
        this.counselorCode = counselorCode;
    }
}
