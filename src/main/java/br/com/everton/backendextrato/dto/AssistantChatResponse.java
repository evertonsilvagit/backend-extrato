package br.com.everton.backendextrato.dto;

import java.util.List;

public record AssistantChatResponse(
        String answer,
        List<String> suggestions,
        String mode
) {
}
