package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.CategoriaDividaDto;
import br.com.everton.backendextrato.model.CategoriaDivida;
import br.com.everton.backendextrato.repository.CategoriaDividaRepository;
import br.com.everton.backendextrato.repository.DividaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaDividaService {

    private final CategoriaDividaRepository categoriaRepository;
    private final DividaRepository dividaRepository;

    public CategoriaDividaService(CategoriaDividaRepository categoriaRepository, DividaRepository dividaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.dividaRepository = dividaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaDividaDto> listar(String userEmail) {
        return categoriaRepository.findAllByUserEmailIgnoreCaseOrderByNomeAsc(userEmail).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CategoriaDividaDto salvar(String userEmail, CategoriaDividaDto dto) {
        validar(dto);

        CategoriaDivida categoria;
        if (dto.id() != null) {
            categoria = categoriaRepository.findByIdAndUserEmailIgnoreCase(dto.id(), userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Categoria de dívida não encontrada para o usuário autenticado."));
        } else {
            categoria = new CategoriaDivida();
        }

        categoriaRepository.findByUserEmailIgnoreCaseAndNomeIgnoreCase(userEmail, dto.nome())
                .filter(existing -> !existing.getId().equals(categoria.getId()))
                .ifPresent(existing -> {
                    throw new IllegalStateException("Já existe uma categoria de dívida com este nome.");
                });

        categoria.setNome(dto.nome().trim());
        categoria.setUserEmail(userEmail);

        return toDto(categoriaRepository.save(categoria));
    }

    @Transactional
    public boolean remover(String userEmail, Long id) {
        CategoriaDivida categoria = categoriaRepository.findByIdAndUserEmailIgnoreCase(id, userEmail)
                .orElse(null);

        if (categoria == null) {
            return false;
        }

        if (dividaRepository.existsByCategoria_IdAndUserEmailIgnoreCase(id, userEmail)) {
            throw new IllegalStateException("Não é possível excluir uma categoria de dívida que está em uso.");
        }

        categoriaRepository.delete(categoria);
        return true;
    }

    private CategoriaDividaDto toDto(CategoriaDivida categoria) {
        return new CategoriaDividaDto(categoria.getId(), categoria.getNome());
    }

    private void validar(CategoriaDividaDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Payload obrigatório.");
        }

        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório.");
        }
    }
}
