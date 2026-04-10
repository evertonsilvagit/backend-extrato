package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.dto.OperationRuntimeResponse;
import br.com.everton.backendextrato.service.OperationRuntimeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operacoes")
public class OperationController {

    private final OperationRuntimeService operationRuntimeService;

    public OperationController(OperationRuntimeService operationRuntimeService) {
        this.operationRuntimeService = operationRuntimeService;
    }

    @GetMapping("/runtime")
    public ResponseEntity<OperationRuntimeResponse> runtime() {
        return ResponseEntity.ok(operationRuntimeService.snapshot());
    }
}
