package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.DividaDto;
import br.com.everton.backendextrato.model.CategoriaDivida;
import br.com.everton.backendextrato.model.Divida;
import br.com.everton.backendextrato.repository.CategoriaDividaRepository;
import br.com.everton.backendextrato.repository.DividaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class DividaService {

    private final DividaRepository repository;
    private final CategoriaDividaRepository categoriaRepository;

    public DividaService(DividaRepository repository, CategoriaDividaRepository categoriaRepository) {
        this.repository = repository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public DividaDto salvar(String userEmail, DividaDto dto) {
        validar(dto);

        Divida entity;
        if (dto.id() != null) {
            entity = repository.findByIdAndUserEmailIgnoreCase(dto.id(), userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Divida nao encontrada para o usuario autenticado."));
        } else {
            entity = new Divida();
        }

        CategoriaDivida categoria = categoriaRepository.findByUserEmailIgnoreCaseAndNomeIgnoreCase(userEmail, dto.categoria().trim())
                .orElseThrow(() -> new IllegalArgumentException("Categoria de divida invalida para o usuario autenticado."));

        entity.setDescricao(dto.descricao().trim());
        entity.setValor(dto.valor());
        entity.setCategoria(categoria);
        entity.setOrdem(resolveOrder(userEmail, entity, dto.ordem()));
        entity.setUserEmail(userEmail);

        Divida saved = repository.save(entity);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<DividaDto> listar(String userEmail) {
        return repository.findAllByUserEmailIgnoreCaseOrderByOrdemAscDescricaoAscIdAsc(userEmail).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public boolean remover(String userEmail, Long id) {
        Optional<Divida> divida = repository.findByIdAndUserEmailIgnoreCase(id, userEmail);
        if (divida.isEmpty()) {
            return false;
        }

        repository.delete(divida.get());
        return true;
    }

    private DividaDto toDto(Divida entity) {
        return new DividaDto(
                entity.getId(),
                entity.getDescricao(),
                entity.getValor(),
                entity.getCategoria() != null ? entity.getCategoria().getNome() : "Sem categoria",
                entity.getOrdem()
        );
    }

    private Integer resolveOrder(String userEmail, Divida entity, Integer requestedOrder) {
        if (requestedOrder != null) {
            return requestedOrder;
        }

        if (entity.getOrdem() != null) {
            return entity.getOrdem();
        }

        return repository.findTopByUserEmailIgnoreCaseOrderByOrdemDescIdDesc(userEmail)
                .map(existing -> existing.getOrdem() + 1)
                .orElse(1);
    }

    private void validar(DividaDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Payload obrigatorio.");
        }
        if (dto.descricao() == null || dto.descricao().isBlank()) {
            throw new IllegalArgumentException("Descricao obrigatoria");
        }
        if (dto.valor() == null || dto.valor().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Valor invalido");
        }
        if (dto.categoria() == null || dto.categoria().isBlank()) {
            throw new IllegalArgumentException("Categoria obrigatoria");
        }
        if (dto.ordem() != null && dto.ordem() < 1) {
            throw new IllegalArgumentException("Ordem invalida");
        }
    }
}
