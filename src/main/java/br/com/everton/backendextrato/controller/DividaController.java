package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.dto.DividaDto;
import br.com.everton.backendextrato.service.DividaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dividas")
public class DividaController {

    private final DividaService service;

    public DividaController(DividaService service) {
        this.service = service;
    }

    @GetMapping
    public List<DividaDto> listar() {
        return service.listar();
    }

    @PostMapping
    public ResponseEntity<DividaDto> salvar(@RequestBody DividaDto dto) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        return service.remover(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
