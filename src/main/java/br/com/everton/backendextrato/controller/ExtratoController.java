package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.dto.CreateLancamentoRequest;
import br.com.everton.backendextrato.dto.ExtratoResponse;
import br.com.everton.backendextrato.dto.LancamentoDto;
import br.com.everton.backendextrato.service.ExtratoService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/api/extratos")
public class ExtratoController {

    private final ExtratoService service;

    public ExtratoController(ExtratoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ExtratoResponse> listar(
            @RequestParam Long contaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(service.listarExtrato(contaId, de, ate, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LancamentoDto> obter(@PathVariable Long id) {
        Optional<LancamentoDto> dto = service.buscarPorId(id);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LancamentoDto> criar(@RequestBody CreateLancamentoRequest request) {
        try {
            LancamentoDto criado = service.criarLancamento(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        boolean removed = service.remover(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
