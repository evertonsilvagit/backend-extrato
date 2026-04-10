package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.dto.InvoiceRecordDto;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.service.AccessControlService;
import br.com.everton.backendextrato.service.InvoiceRecordService;
import br.com.everton.backendextrato.service.MongoAuditEventService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notas-fiscais")
public class InvoiceRecordController {

    private final InvoiceRecordService service;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final AccessControlService accessControlService;
    private final MongoAuditEventService auditEventService;

    public InvoiceRecordController(
            InvoiceRecordService service,
            AuthenticatedUserResolver authenticatedUserResolver,
            AccessControlService accessControlService,
            MongoAuditEventService auditEventService
    ) {
        this.service = service;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.accessControlService = accessControlService;
        this.auditEventService = auditEventService;
    }

    @GetMapping
    public ResponseEntity<?> list(
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveReadableOwner(user.email(), ownerEmail);
            return ResponseEntity.ok(service.list(effectiveOwnerEmail));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @PostMapping("/import")
    public ResponseEntity<?> importRecords(
            @RequestBody List<InvoiceRecordDto> request,
            @RequestParam(required = false) String ownerEmail,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String effectiveOwnerEmail = accessControlService.resolveWritableOwner(user.email(), ownerEmail);
            var response = service.importRecords(effectiveOwnerEmail, request);
            auditEventService.record(
                    effectiveOwnerEmail,
                    "invoice-import",
                    "invoice-records",
                    "api",
                    Map.of(
                            "created", response.created(),
                            "updated", response.updated(),
                            "total", response.total()
                    )
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new NotificationErrorResponse(ex.getMessage()));
        }
    }
}
