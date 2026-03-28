package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.dto.AccessManagementResponse;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.dto.ShareAccessRequest;
import br.com.everton.backendextrato.dto.SharedViewerResponse;
import br.com.everton.backendextrato.service.AccessControlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accessos")
public class AccessControlController {

    private final AccessControlService accessControlService;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public AccessControlController(
            AccessControlService accessControlService,
            AuthenticatedUserResolver authenticatedUserResolver
    ) {
        this.accessControlService = accessControlService;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    @GetMapping
    public ResponseEntity<AccessManagementResponse> list(HttpServletRequest httpServletRequest) {
        AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
        return ResponseEntity.ok(accessControlService.getManagementData(user.email()));
    }

    @PostMapping
    public ResponseEntity<?> grant(@RequestBody ShareAccessRequest request, HttpServletRequest httpServletRequest) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            SharedViewerResponse response = accessControlService.grantAccess(user.email(), request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> revoke(@PathVariable Long id, HttpServletRequest httpServletRequest) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            accessControlService.revokeAccess(user.email(), id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new NotificationErrorResponse(ex.getMessage()));
        }
    }
}
