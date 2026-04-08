package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.dto.AssistantChatRequest;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.service.AccessControlService;
import br.com.everton.backendextrato.service.AssistantChatService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class AssistantChatController {

    private final AssistantChatService assistantChatService;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AccessControlService accessControlService;

    public AssistantChatController(
            AssistantChatService assistantChatService,
            AuthenticatedUserResolver authenticatedUserResolver,
            AccessControlService accessControlService
    ) {
        this.assistantChatService = assistantChatService;
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
            return ResponseEntity.ok(
                    assistantChatService.reply(user.name(), effectiveOwnerEmail, request.messages())
            );
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new NotificationErrorResponse(ex.getMessage()));
        }
    }
}
