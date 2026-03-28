package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.dto.ContaDto;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.service.AccessControlService;
import br.com.everton.backendextrato.service.ContaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/contas")
public class ContaController {

    private final ContaService service;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AccessControlService accessControlService;

    public ContaController(
            ContaService service,
            AuthenticatedUserResolver authenticatedUserResolver,
            AccessControlService accessControlService
    ) {
        this.service = service;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.accessControlService = accessControlService;
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @RequestBody ContaDto request,
            @RequestHeader(value = "X-Owner-Email", required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveWritableOwner(user.email(), ownerEmail);
            ContaDto criado = service.criar(effectiveOwnerEmail, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestHeader(value = "X-Owner-Email", required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveReadableOwner(user.email(), ownerEmail);
            return ResponseEntity.ok(service.listar(effectiveOwnerEmail));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obter(
            @PathVariable Long id,
            @RequestHeader(value = "X-Owner-Email", required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveReadableOwner(user.email(), ownerEmail);
            return service.buscarPorId(effectiveOwnerEmail, id)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> remover(
            @PathVariable Long id,
            @RequestHeader(value = "X-Owner-Email", required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveWritableOwner(user.email(), ownerEmail);
            boolean removed = service.remover(effectiveOwnerEmail, id);
            return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }
}
