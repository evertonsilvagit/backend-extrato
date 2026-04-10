package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.application.categoriadivida.port.in.DeleteDebtCategoryUseCase;
import br.com.everton.backendextrato.application.categoriadivida.port.in.ListDebtCategoriesUseCase;
import br.com.everton.backendextrato.application.categoriadivida.port.in.SaveDebtCategoryUseCase;
import br.com.everton.backendextrato.application.categoriadivida.usecase.command.SaveDebtCategoryCommand;
import br.com.everton.backendextrato.domain.categoriadivida.DebtCategory;
import br.com.everton.backendextrato.dto.CategoriaDividaDto;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.service.AccessControlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias-divida")
public class CategoriaDividaController {

    private final ListDebtCategoriesUseCase listDebtCategoriesUseCase;
    private final SaveDebtCategoryUseCase saveDebtCategoryUseCase;
    private final DeleteDebtCategoryUseCase deleteDebtCategoryUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AccessControlService accessControlService;

    public CategoriaDividaController(
            ListDebtCategoriesUseCase listDebtCategoriesUseCase,
            SaveDebtCategoryUseCase saveDebtCategoryUseCase,
            DeleteDebtCategoryUseCase deleteDebtCategoryUseCase,
            AuthenticatedUserResolver authenticatedUserResolver,
            AccessControlService accessControlService
    ) {
        this.listDebtCategoriesUseCase = listDebtCategoriesUseCase;
        this.saveDebtCategoryUseCase = saveDebtCategoryUseCase;
        this.deleteDebtCategoryUseCase = deleteDebtCategoryUseCase;
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
            return ResponseEntity.ok(listDebtCategoriesUseCase.execute(effectiveOwnerEmail).stream()
                    .map(this::toDto)
                    .toList());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> salvar(
            @RequestBody CategoriaDividaDto dto,
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveWritableOwner(user.email(), ownerEmail);
            DebtCategory saved = saveDebtCategoryUseCase.execute(
                    new SaveDebtCategoryCommand(dto.id(), dto.nome(), effectiveOwnerEmail)
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
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
            boolean removed = deleteDebtCategoryUseCase.execute(effectiveOwnerEmail, id);
            return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new NotificationErrorResponse(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    private CategoriaDividaDto toDto(DebtCategory category) {
        return new CategoriaDividaDto(category.id(), category.name());
    }
}
