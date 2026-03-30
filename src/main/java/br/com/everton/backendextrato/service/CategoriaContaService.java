package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.CategoriaContaDto;
import br.com.everton.backendextrato.model.CategoriaConta;
import br.com.everton.backendextrato.repository.CategoriaContaRepository;
import br.com.everton.backendextrato.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoriaContaService {

    private final CategoriaContaRepository categoriaRepository;
    private final ContaRepository contaRepository;

    public CategoriaContaService(CategoriaContaRepository categoriaRepository, ContaRepository contaRepository) {
        this.categoriaRepository = categoriaRepository;
        this.contaRepository = contaRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoriaContaDto> listar(String userEmail) {
        return categoriaRepository.findAllByUserEmailIgnoreCaseOrderByNomeAsc(userEmail).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public CategoriaContaDto salvar(String userEmail, CategoriaContaDto dto) {
        validar(dto);

        CategoriaConta categoria;
        if (dto.id() != null) {
            categoria = categoriaRepository.findByIdAndUserEmailIgnoreCase(dto.id(), userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Categoria de conta não encontrada para o usuário autenticado."));
        } else {
            categoria = new CategoriaConta();
        }

        categoriaRepository.findByUserEmailIgnoreCaseAndNomeIgnoreCase(userEmail, dto.nome())
                .filter(existing -> !existing.getId().equals(categoria.getId()))
                .ifPresent(existing -> {
                    throw new IllegalStateException("Já existe uma categoria de conta com este nome.");
                });

        categoria.setNome(dto.nome().trim());
        categoria.setUserEmail(userEmail);

        return toDto(categoriaRepository.save(categoria));
    }

    @Transactional
    public boolean remover(String userEmail, Long id) {
        CategoriaConta categoria = categoriaRepository.findByIdAndUserEmailIgnoreCase(id, userEmail).orElse(null);
        if (categoria == null) {
            return false;
        }

        if (contaRepository.existsByCategoria_IdAndUserEmailIgnoreCase(id, userEmail)) {
            throw new IllegalStateException("Não é possível excluir uma categoria de conta que está em uso.");
        }

        categoriaRepository.delete(categoria);
        return true;
    }

    private CategoriaContaDto toDto(CategoriaConta categoria) {
        return new CategoriaContaDto(categoria.getId(), categoria.getNome());
    }

    private void validar(CategoriaContaDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Payload obrigatório.");
        }
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new IllegalArgumentException("Nome da categoria é obrigatório.");
        }
    }
}
