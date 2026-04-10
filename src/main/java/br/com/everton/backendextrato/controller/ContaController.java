package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.application.conta.port.in.DeleteAccountBillUseCase;
import br.com.everton.backendextrato.application.conta.port.in.GetAccountBillUseCase;
import br.com.everton.backendextrato.application.conta.port.in.ListAccountBillsUseCase;
import br.com.everton.backendextrato.application.conta.port.in.SaveAccountBillUseCase;
import br.com.everton.backendextrato.application.conta.usecase.command.SaveAccountBillCommand;
import br.com.everton.backendextrato.domain.conta.AccountBill;
import br.com.everton.backendextrato.dto.ContaDto;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.service.AccessControlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/contas")
public class ContaController {

    private final SaveAccountBillUseCase saveAccountBillUseCase;
    private final ListAccountBillsUseCase listAccountBillsUseCase;
    private final GetAccountBillUseCase getAccountBillUseCase;
    private final DeleteAccountBillUseCase deleteAccountBillUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AccessControlService accessControlService;

    public ContaController(
            SaveAccountBillUseCase saveAccountBillUseCase,
            ListAccountBillsUseCase listAccountBillsUseCase,
            GetAccountBillUseCase getAccountBillUseCase,
            DeleteAccountBillUseCase deleteAccountBillUseCase,
            AuthenticatedUserResolver authenticatedUserResolver,
            AccessControlService accessControlService
    ) {
        this.saveAccountBillUseCase = saveAccountBillUseCase;
        this.listAccountBillsUseCase = listAccountBillsUseCase;
        this.getAccountBillUseCase = getAccountBillUseCase;
        this.deleteAccountBillUseCase = deleteAccountBillUseCase;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.accessControlService = accessControlService;
    }

    @PostMapping
    public ResponseEntity<?> criar(
            @RequestBody ContaDto request,
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveWritableOwner(user.email(), ownerEmail);
            AccountBill created = saveAccountBillUseCase.execute(toCommand(request, effectiveOwnerEmail));
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveReadableOwner(user.email(), ownerEmail);
            return ResponseEntity.ok(listAccountBillsUseCase.execute(effectiveOwnerEmail).stream()
                    .map(this::toDto)
                    .toList());
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
            return getAccountBillUseCase.execute(effectiveOwnerEmail, id)
                    .map(this::toDto)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @PathVariable Long id,
            @RequestBody ContaDto request,
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveWritableOwner(user.email(), ownerEmail);
            ContaDto payload = new ContaDto(
                    id,
                    request.descricao(),
                    request.valor(),
                    request.diaPagamento(),
                    request.categoria(),
                    request.mesesVigencia(),
                    request.ordem()
            );
            AccountBill updated = saveAccountBillUseCase.execute(toCommand(payload, effectiveOwnerEmail));
            return ResponseEntity.ok(toDto(updated));
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
            boolean removed = deleteAccountBillUseCase.execute(effectiveOwnerEmail, id);
            return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    private SaveAccountBillCommand toCommand(ContaDto dto, String ownerEmail) {
        return new SaveAccountBillCommand(
                dto.id(),
                dto.descricao(),
                dto.valor(),
                dto.diaPagamento(),
                dto.categoria(),
                dto.mesesVigencia(),
                dto.ordem(),
                ownerEmail
        );
    }

    private ContaDto toDto(AccountBill bill) {
        return new ContaDto(
                bill.id(),
                bill.description(),
                bill.amount(),
                bill.paymentDay(),
                bill.categoryName(),
                bill.activeMonths(),
                bill.sortOrder()
        );
    }
}
