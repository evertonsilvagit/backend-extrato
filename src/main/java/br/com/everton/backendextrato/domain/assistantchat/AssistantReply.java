package br.com.everton.backendextrato.domain.assistantchat;

import java.util.List;

public record AssistantReply(
        String answer,
        List<String> suggestions,
        String mode
) {}
