package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.dto.CreateEntradaRequest;
import br.com.everton.backendextrato.dto.EntradaDto;
import br.com.everton.backendextrato.service.EntradaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/entradas")
public class EntradaController {

    private final EntradaService service;

    public EntradaController(EntradaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<EntradaDto> criar(@RequestBody CreateEntradaRequest request) {
        try {
            EntradaDto criado = service.criar(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(criado);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntradaDto> obter(@PathVariable Long id) {
        Optional<EntradaDto> dto = service.buscarPorId(id);
        return dto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<EntradaDto>> listar(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        return ResponseEntity.ok(service.listar(page, size));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        boolean removed = service.remover(id);
        return removed ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
