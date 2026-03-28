package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.dto.DividaDto;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.service.AccessControlService;
import br.com.everton.backendextrato.service.DividaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dividas")
public class DividaController {

    private final DividaService service;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AccessControlService accessControlService;

    public DividaController(
            DividaService service,
            AuthenticatedUserResolver authenticatedUserResolver,
            AccessControlService accessControlService
    ) {
        this.service = service;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String ownerEmail,
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

    @PostMapping
    public ResponseEntity<?> salvar(
            @RequestBody DividaDto dto,
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveWritableOwner(user.email(), ownerEmail);
            return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(effectiveOwnerEmail, dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> remover(
            @PathVariable Long id,
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveWritableOwner(user.email(), ownerEmail);
            return service.remover(effectiveOwnerEmail, id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(e.getMessage()));
        }
    }
}
