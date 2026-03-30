package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.dto.CategoriaContaDto;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.service.AccessControlService;
import br.com.everton.backendextrato.service.CategoriaContaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias-conta")
public class CategoriaContaController {

    private final CategoriaContaService service;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AccessControlService accessControlService;

    public CategoriaContaController(
            CategoriaContaService service,
            AuthenticatedUserResolver authenticatedUserResolver,
            AccessControlService accessControlService
    ) {
        this.service = service;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String ownerEmail, HttpServletRequest httpServletRequest) {
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
            @RequestBody CategoriaContaDto dto,
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveWritableOwner(user.email(), ownerEmail);
            CategoriaContaDto salvo = service.salvar(effectiveOwnerEmail, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new NotificationErrorResponse(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
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
            boolean removed = service.remover(effectiveOwnerEmail, id);
            return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new NotificationErrorResponse(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }
}
