package com.campus.trading.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiAssistantServiceTest {

    @Test
    void generatedDescriptionIsTruncatedAtPunctuationWithinLimit() {
        LlmClient llmClient = mock(LlmClient.class);
        String generated = ("商品状态良好，功能正常，适合日常使用。").repeat(20);
        when(llmClient.ask(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(generated);
        AiAssistantService service = new AiAssistantService(
                mock(KnowledgeBase.class), llmClient, new ObjectMapper(), new SensitiveWordFilter());

        String description = service.generateProductDescription("iPhone 13", "数码");

        assertTrue(description.codePointCount(0, description.length()) <= 200);
        assertTrue("。！？；，".indexOf(description.codePointBefore(description.length())) >= 0);
        assertTrue(description.length() < generated.length());
    }

    @Test
    void generatedDescriptionShorterThanFiveCharactersFails() {
        LlmClient llmClient = mock(LlmClient.class);
        when(llmClient.ask(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn("好用。");
        AiAssistantService service = new AiAssistantService(
                mock(KnowledgeBase.class), llmClient, new ObjectMapper(), new SensitiveWordFilter());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> service.generateProductDescription("iPhone 13", "数码"));

        assertEquals("大模型生成的商品描述少于 5 字，请重试", exception.getMessage());
    }

    @Test
    void auditRejectsLocalViolationWithoutCallingLlm() {
        LlmClient llmClient = mock(LlmClient.class);
        AiAssistantService service = new AiAssistantService(
                mock(KnowledgeBase.class), llmClient, new ObjectMapper(), new SensitiveWordFilter());

        AiAssistantService.AuditResult result = service.auditProduct("二手书", "需要代写作业请联系");

        assertEquals(false, result.passed());
        assertTrue(result.reason().contains("代写"));
        org.mockito.Mockito.verifyNoInteractions(llmClient);
    }

    @Test
    void auditUsesAiAndAllowsWhenAiServiceFails() {
        LlmClient llmClient = mock(LlmClient.class);
        when(llmClient.ask(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString()))
                .thenThrow(new IllegalStateException("service unavailable"));
        AiAssistantService service = new AiAssistantService(
                mock(KnowledgeBase.class), llmClient, new ObjectMapper(), new SensitiveWordFilter());

        AiAssistantService.AuditResult result = service.auditProduct("二手教材", "保存完好，正常使用。");

        assertEquals(true, result.passed());
    }
}
