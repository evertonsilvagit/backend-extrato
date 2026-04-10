package br.com.everton.backendextrato.infrastructure.divida;

import br.com.everton.backendextrato.application.divida.exception.DebtValidationException;
import br.com.everton.backendextrato.application.divida.port.out.DebtRepository;
import br.com.everton.backendextrato.domain.divida.Debt;
import br.com.everton.backendextrato.model.CategoriaDivida;
import br.com.everton.backendextrato.model.Divida;
import br.com.everton.backendextrato.repository.CategoriaDividaRepository;
import br.com.everton.backendextrato.repository.DividaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DebtJpaAdapter implements DebtRepository {

    private final DividaRepository dividaRepository;
    private final CategoriaDividaRepository categoriaDividaRepository;

    public DebtJpaAdapter(DividaRepository dividaRepository, CategoriaDividaRepository categoriaDividaRepository) {
        this.dividaRepository = dividaRepository;
        this.categoriaDividaRepository = categoriaDividaRepository;
    }

    @Override
    public Optional<Debt> findByIdAndOwnerEmail(Long id, String ownerEmail) {
        return dividaRepository.findByIdAndUserEmailIgnoreCase(id, ownerEmail).map(this::toDomain);
    }

    @Override
    public List<Debt> findAllByOwnerEmail(String ownerEmail) {
        return dividaRepository.findAllByUserEmailIgnoreCaseOrderByOrdemAscDescricaoAscIdAsc(ownerEmail).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Debt> findTopByOwnerEmailOrderBySortOrderDesc(String ownerEmail) {
        return dividaRepository.findTopByUserEmailIgnoreCaseOrderByOrdemDescIdDesc(ownerEmail).map(this::toDomain);
    }

    @Override
    public Debt save(Debt debt) {
        Divida entity = debt.id() != null ? dividaRepository.findById(debt.id()).orElseGet(Divida::new) : new Divida();
        CategoriaDivida category = categoriaDividaRepository.findById(debt.categoryId())
                .orElseThrow(() -> new DebtValidationException("Categoria de divida invalida para o usuario autenticado."));

        entity.setDescricao(debt.description());
        entity.setValor(debt.amount());
        entity.setCategoria(category);
        entity.setOrdem(debt.sortOrder());
        entity.setUserEmail(debt.ownerEmail());

        return toDomain(dividaRepository.save(entity));
    }

    @Override
    public void delete(Debt debt) {
        dividaRepository.findById(debt.id()).ifPresent(dividaRepository::delete);
    }

    private Debt toDomain(Divida entity) {
        return new Debt(
                entity.getId(),
                entity.getDescricao(),
                entity.getValor(),
                entity.getCategoria() != null ? entity.getCategoria().getId() : null,
                entity.getCategoria() != null ? entity.getCategoria().getNome() : "Sem categoria",
                entity.getOrdem(),
                entity.getUserEmail()
        );
    }
}
