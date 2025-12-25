package com.example.psyaihealer.aiconfig;

import jakarta.persistence.*;

@Entity
@Table(name = "ai_engine_config")
public class AiEngineConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 64)
    private String avatarName;

    @Column(length = 64)
    private String voice;

    @Column(length = 4000)
    private String dialogRules; // JSON or text

    public AiEngineConfig() {}

    public AiEngineConfig(String avatarName, String voice, String dialogRules) {
        this.avatarName = avatarName;
        this.voice = voice;
        this.dialogRules = dialogRules;
    }

    public Long getId() { return id; }
    public String getAvatarName() { return avatarName; }
    public void setAvatarName(String avatarName) { this.avatarName = avatarName; }
    public String getVoice() { return voice; }
    public void setVoice(String voice) { this.voice = voice; }
    public String getDialogRules() { return dialogRules; }
    public void setDialogRules(String dialogRules) { this.dialogRules = dialogRules; }
}
