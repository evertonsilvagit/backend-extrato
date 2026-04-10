package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.application.divida.port.in.DeleteDebtUseCase;
import br.com.everton.backendextrato.application.divida.port.in.ListDebtsUseCase;
import br.com.everton.backendextrato.application.divida.port.in.SaveDebtUseCase;
import br.com.everton.backendextrato.application.divida.usecase.command.SaveDebtCommand;
import br.com.everton.backendextrato.domain.divida.Debt;
import br.com.everton.backendextrato.dto.DividaDto;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.service.AccessControlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dividas")
public class DividaController {

    private final ListDebtsUseCase listDebtsUseCase;
    private final SaveDebtUseCase saveDebtUseCase;
    private final DeleteDebtUseCase deleteDebtUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AccessControlService accessControlService;

    public DividaController(
            ListDebtsUseCase listDebtsUseCase,
            SaveDebtUseCase saveDebtUseCase,
            DeleteDebtUseCase deleteDebtUseCase,
            AuthenticatedUserResolver authenticatedUserResolver,
            AccessControlService accessControlService
    ) {
        this.listDebtsUseCase = listDebtsUseCase;
        this.saveDebtUseCase = saveDebtUseCase;
        this.deleteDebtUseCase = deleteDebtUseCase;
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
            return ResponseEntity.ok(listDebtsUseCase.execute(effectiveOwnerEmail).stream().map(this::toDto).toList());
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
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    toDto(saveDebtUseCase.execute(toCommand(dto, effectiveOwnerEmail)))
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @RequestBody DividaDto dto,
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveWritableOwner(user.email(), ownerEmail);
            DividaDto payload = new DividaDto(
                    id,
                    dto.descricao(),
                    dto.valor(),
                    dto.categoria(),
                    dto.ordem()
            );
            return ResponseEntity.ok(toDto(saveDebtUseCase.execute(toCommand(payload, effectiveOwnerEmail))));
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
            return deleteDebtUseCase.execute(effectiveOwnerEmail, id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(e.getMessage()));
        }
    }

    private SaveDebtCommand toCommand(DividaDto dto, String ownerEmail) {
        return new SaveDebtCommand(dto.id(), dto.descricao(), dto.valor(), dto.categoria(), dto.ordem(), ownerEmail);
    }

    private DividaDto toDto(Debt debt) {
        return new DividaDto(debt.id(), debt.description(), debt.amount(), debt.categoryName(), debt.sortOrder());
    }
}
