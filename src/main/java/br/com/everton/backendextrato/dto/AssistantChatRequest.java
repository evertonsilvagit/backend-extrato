package br.com.everton.backendextrato.dto;

import java.util.List;

public record AssistantChatRequest(
        List<AssistantChatMessageRequest> messages
) {
}
