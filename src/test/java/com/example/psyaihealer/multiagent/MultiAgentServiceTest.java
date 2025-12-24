package com.example.psyaihealer.multiagent;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiAgentServiceTest {

    @Test
    void shouldGenerateStrategyAndReply() {
        MultiAgentService service = new MultiAgentService();
        MultiAgentService.AgentResult result = service.process("我最近有些焦虑");
        assertThat(result.reply()).isNotBlank();
        assertThat(result.strategy()).isNotBlank();
    }
}
