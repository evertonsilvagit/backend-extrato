package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.DividaDto;
import br.com.everton.backendextrato.model.Divida;
import br.com.everton.backendextrato.repository.DividaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DividaService {

    private final DividaRepository repository;

    public DividaService(DividaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public DividaDto salvar(String userEmail, DividaDto dto) {
        validar(dto);
        Divida entity;
        if (dto.id() != null) {
            entity = repository.findByIdAndUserEmailIgnoreCase(dto.id(), userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Dívida não encontrada para o usuário autenticado."));
        } else {
            entity = new Divida();
        }

        entity.setDescricao(dto.descricao());
        entity.setValor(dto.valor());
        entity.setGrupo(dto.grupo());
        entity.setUserEmail(userEmail);

        Divida salvo = repository.save(entity);
        return toDto(salvo);
    }

    @Transactional(readOnly = true)
    public List<DividaDto> listar(String userEmail) {
        return repository.findAllByUserEmailIgnoreCase(userEmail).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean remover(String userEmail, Long id) {
        Optional<Divida> divida = repository.findByIdAndUserEmailIgnoreCase(id, userEmail);
        if (divida.isEmpty()) return false;
        repository.delete(divida.get());
        return true;
    }

    private DividaDto toDto(Divida entity) {
        return new DividaDto(
                entity.getId(),
                entity.getDescricao(),
                entity.getValor(),
                entity.getGrupo()
        );
    }

    private void validar(DividaDto dto) {
        if (dto.descricao() == null || dto.descricao().isBlank()) throw new IllegalArgumentException("Descrição obrigatória");
        if (dto.valor() == null || dto.valor().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("Valor inválido");
        if (dto.grupo() == null || dto.grupo().isBlank()) throw new IllegalArgumentException("Grupo obrigatório");
    }
}
