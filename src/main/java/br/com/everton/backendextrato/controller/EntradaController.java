package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.application.entrada.port.in.DeleteIncomeEntryUseCase;
import br.com.everton.backendextrato.application.entrada.port.in.GetIncomeEntryUseCase;
import br.com.everton.backendextrato.application.entrada.port.in.ListIncomeEntriesUseCase;
import br.com.everton.backendextrato.application.entrada.port.in.SaveIncomeEntryUseCase;
import br.com.everton.backendextrato.application.entrada.usecase.command.SaveIncomeEntryCommand;
import br.com.everton.backendextrato.domain.entrada.IncomeEntry;
import br.com.everton.backendextrato.dto.CreateEntradaRequest;
import br.com.everton.backendextrato.dto.EntradaDto;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.service.AccessControlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entradas")
public class EntradaController {

    private final SaveIncomeEntryUseCase saveIncomeEntryUseCase;
    private final GetIncomeEntryUseCase getIncomeEntryUseCase;
    private final ListIncomeEntriesUseCase listIncomeEntriesUseCase;
    private final DeleteIncomeEntryUseCase deleteIncomeEntryUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AccessControlService accessControlService;

    public EntradaController(
            SaveIncomeEntryUseCase saveIncomeEntryUseCase,
            GetIncomeEntryUseCase getIncomeEntryUseCase,
            ListIncomeEntriesUseCase listIncomeEntriesUseCase,
            DeleteIncomeEntryUseCase deleteIncomeEntryUseCase,
            AuthenticatedUserResolver authenticatedUserResolver,
            AccessControlService accessControlService
    ) {
        this.saveIncomeEntryUseCase = saveIncomeEntryUseCase;
        this.getIncomeEntryUseCase = getIncomeEntryUseCase;
        this.listIncomeEntriesUseCase = listIncomeEntriesUseCase;
        this.deleteIncomeEntryUseCase = deleteIncomeEntryUseCase;
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
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    toDto(saveIncomeEntryUseCase.execute(toCommand(request, effectiveOwnerEmail)))
            );
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
            return getIncomeEntryUseCase.execute(effectiveOwnerEmail, id)
                    .map(this::toDto)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound().build());
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
            return ResponseEntity.ok(listIncomeEntriesUseCase.execute(effectiveOwnerEmail, page, size).stream()
                    .map(this::toDto)
                    .toList());
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
            return ResponseEntity.ok(toDto(saveIncomeEntryUseCase.execute(toCommand(payload, effectiveOwnerEmail))));
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
            boolean removed = deleteIncomeEntryUseCase.execute(effectiveOwnerEmail, id);
            return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    private SaveIncomeEntryCommand toCommand(CreateEntradaRequest request, String ownerEmail) {
        return new SaveIncomeEntryCommand(
                request.id(),
                request.nome(),
                request.tipo(),
                request.valor(),
                request.taxaImposto(),
                request.diasRecebimento(),
                request.valorLiquido(),
                request.categoriaRecebimento(),
                request.mesesVigencia(),
                request.ordem(),
                ownerEmail
        );
    }

    private EntradaDto toDto(IncomeEntry incomeEntry) {
        return new EntradaDto(
                incomeEntry.id(),
                incomeEntry.name(),
                incomeEntry.type(),
                incomeEntry.amount(),
                incomeEntry.taxRate(),
                incomeEntry.paymentDays(),
                incomeEntry.netAmount(),
                incomeEntry.incomeCategory(),
                incomeEntry.activeMonths(),
                incomeEntry.sortOrder()
        );
    }
}
