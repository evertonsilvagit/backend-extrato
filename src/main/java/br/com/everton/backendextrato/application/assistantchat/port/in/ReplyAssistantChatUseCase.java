package br.com.everton.backendextrato.application.assistantchat.port.in;

import br.com.everton.backendextrato.dto.AssistantChatMessageRequest;
import br.com.everton.backendextrato.domain.assistantchat.AssistantReply;

import java.util.List;

public interface ReplyAssistantChatUseCase {
    AssistantReply execute(String userName, String ownerEmail, List<AssistantChatMessageRequest> messages);
}
