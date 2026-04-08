package br.com.everton.backendextrato.dto;

public record AssistantChatMessageRequest(
        String role,
        String content
) {
}
