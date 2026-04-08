package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.dto.CreateEntradaRequest;
import br.com.everton.backendextrato.dto.EntradaDto;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.service.AccessControlService;
import br.com.everton.backendextrato.service.EntradaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/entradas")
public class EntradaController {

    private final EntradaService service;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AccessControlService accessControlService;

    public EntradaController(
            EntradaService service,
            AuthenticatedUserResolver authenticatedUserResolver,
            AccessControlService accessControlService
    ) {
        this.service = service;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.accessControlService = accessControlService;
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @RequestBody CreateEntradaRequest request,
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveWritableOwner(user.email(), ownerEmail);
            EntradaDto criado = service.criar(effectiveOwnerEmail, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obter(
            @PathVariable Long id,
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveReadableOwner(user.email(), ownerEmail);
            Optional<EntradaDto> dto = service.buscarPorId(effectiveOwnerEmail, id);
            return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<EntradaDto>> listar(
            @RequestParam(required = false) String ownerEmail,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveReadableOwner(user.email(), ownerEmail);
            return ResponseEntity.ok(service.listar(effectiveOwnerEmail, page, size));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @RequestBody CreateEntradaRequest request,
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveWritableOwner(user.email(), ownerEmail);
            CreateEntradaRequest payload = new CreateEntradaRequest(
                    id,
                    request.nome(),
                    request.tipo(),
                    request.valor(),
                    request.taxaImposto(),
                    request.diasRecebimento(),
                    request.valorLiquido(),
                    request.categoriaRecebimento(),
                    request.mesesVigencia(),
                    request.ordem()
            );
            EntradaDto atualizado = service.criar(effectiveOwnerEmail, payload);
            return ResponseEntity.ok(atualizado);
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
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }
}
