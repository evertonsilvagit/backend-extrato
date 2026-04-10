package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.application.assistantchat.port.in.ReplyAssistantChatUseCase;
import br.com.everton.backendextrato.domain.assistantchat.AssistantReply;
import br.com.everton.backendextrato.dto.AssistantChatRequest;
import br.com.everton.backendextrato.dto.AssistantChatResponse;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.service.AccessControlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class AssistantChatController {

    private final ReplyAssistantChatUseCase replyAssistantChatUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AccessControlService accessControlService;

    public AssistantChatController(
            ReplyAssistantChatUseCase replyAssistantChatUseCase,
            AuthenticatedUserResolver authenticatedUserResolver,
            AccessControlService accessControlService
    ) {
        this.replyAssistantChatUseCase = replyAssistantChatUseCase;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.accessControlService = accessControlService;
    }

    @PostMapping("/assistant")
    public ResponseEntity<?> chat(
            @RequestBody AssistantChatRequest request,
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveReadableOwner(user.email(), ownerEmail);
            AssistantReply reply = replyAssistantChatUseCase.execute(
                    user.name(),
                    effectiveOwnerEmail,
                    request == null ? null : request.messages()
            );
            return ResponseEntity.ok(new AssistantChatResponse(reply.answer(), reply.suggestions(), reply.mode()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new NotificationErrorResponse(ex.getMessage()));
        }
    }
}
