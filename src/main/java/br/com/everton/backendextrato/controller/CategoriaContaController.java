package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.application.categoriaconta.port.in.DeleteAccountCategoryUseCase;
import br.com.everton.backendextrato.application.categoriaconta.port.in.ListAccountCategoriesUseCase;
import br.com.everton.backendextrato.application.categoriaconta.port.in.SaveAccountCategoryUseCase;
import br.com.everton.backendextrato.application.categoriaconta.usecase.command.SaveAccountCategoryCommand;
import br.com.everton.backendextrato.domain.categoriaconta.AccountCategory;
import br.com.everton.backendextrato.dto.CategoriaContaDto;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.service.AccessControlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categorias-conta")
public class CategoriaContaController {

    private final ListAccountCategoriesUseCase listAccountCategoriesUseCase;
    private final SaveAccountCategoryUseCase saveAccountCategoryUseCase;
    private final DeleteAccountCategoryUseCase deleteAccountCategoryUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AccessControlService accessControlService;

    public CategoriaContaController(
            ListAccountCategoriesUseCase listAccountCategoriesUseCase,
            SaveAccountCategoryUseCase saveAccountCategoryUseCase,
            DeleteAccountCategoryUseCase deleteAccountCategoryUseCase,
            AuthenticatedUserResolver authenticatedUserResolver,
            AccessControlService accessControlService
    ) {
        this.listAccountCategoriesUseCase = listAccountCategoriesUseCase;
        this.saveAccountCategoryUseCase = saveAccountCategoryUseCase;
        this.deleteAccountCategoryUseCase = deleteAccountCategoryUseCase;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.accessControlService = accessControlService;
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String ownerEmail, HttpServletRequest httpServletRequest) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveReadableOwner(user.email(), ownerEmail);
            return ResponseEntity.ok(listAccountCategoriesUseCase.execute(effectiveOwnerEmail).stream()
                    .map(this::toDto)
                    .toList());
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
            AccountCategory saved = saveAccountCategoryUseCase.execute(
                    new SaveAccountCategoryCommand(dto.id(), dto.nome(), effectiveOwnerEmail)
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
            boolean removed = deleteAccountCategoryUseCase.execute(effectiveOwnerEmail, id);
            return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new NotificationErrorResponse(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    private CategoriaContaDto toDto(AccountCategory category) {
        return new CategoriaContaDto(category.id(), category.name());
    }
}
