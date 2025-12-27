package com.example.psyaihealer.user;

public enum Role {
    STUDENT("学生用户"),
    COUNSELOR("心理咨询师"),
    ADMIN("系统管理员");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
