package com.example.psyaihealer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.registration")
public class RegistrationProperties {
    private String adminCode = "admin-2025-secret";
    private String therapistCode = "therapist-2025-secret";

    public String getAdminCode() {
        return adminCode;
    }

    public void setAdminCode(String adminCode) {
        this.adminCode = adminCode;
    }

    public String getTherapistCode() {
        return therapistCode;
    }

    public void setTherapistCode(String therapistCode) {
        this.therapistCode = therapistCode;
    }
}
