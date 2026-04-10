package br.com.everton.backendextrato.infrastructure.categoriadivida;

import br.com.everton.backendextrato.application.categoriadivida.port.out.DebtCategoryRepository;
import br.com.everton.backendextrato.application.categoriadivida.port.out.DebtCategoryUsageRepository;
import br.com.everton.backendextrato.domain.categoriadivida.DebtCategory;
import br.com.everton.backendextrato.model.CategoriaDivida;
import br.com.everton.backendextrato.repository.CategoriaDividaRepository;
import br.com.everton.backendextrato.repository.DividaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DebtCategoryJpaAdapter implements DebtCategoryRepository, DebtCategoryUsageRepository {

    private final CategoriaDividaRepository categoriaDividaRepository;
    private final DividaRepository dividaRepository;

    public DebtCategoryJpaAdapter(
            CategoriaDividaRepository categoriaDividaRepository,
            DividaRepository dividaRepository
    ) {
        this.categoriaDividaRepository = categoriaDividaRepository;
        this.dividaRepository = dividaRepository;
    }

    @Override
    public List<DebtCategory> findAllByOwnerEmail(String ownerEmail) {
        return categoriaDividaRepository.findAllByUserEmailIgnoreCaseOrderByNomeAsc(ownerEmail).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<DebtCategory> findByIdAndOwnerEmail(Long id, String ownerEmail) {
        return categoriaDividaRepository.findByIdAndUserEmailIgnoreCase(id, ownerEmail)
                .map(this::toDomain);
    }

    @Override
    public Optional<DebtCategory> findByOwnerEmailAndName(String ownerEmail, String name) {
        return categoriaDividaRepository.findByUserEmailIgnoreCaseAndNomeIgnoreCase(ownerEmail, name)
                .map(this::toDomain);
    }

    @Override
    public DebtCategory save(DebtCategory category) {
        CategoriaDivida entity;
        if (category.id() != null) {
            entity = categoriaDividaRepository.findById(category.id()).orElseGet(CategoriaDivida::new);
        } else {
            entity = new CategoriaDivida();
        }

        entity.setNome(category.name());
        entity.setUserEmail(category.ownerEmail());

        return toDomain(categoriaDividaRepository.save(entity));
    }

    @Override
    public void delete(DebtCategory category) {
        categoriaDividaRepository.findById(category.id())
                .ifPresent(categoriaDividaRepository::delete);
    }

    @Override
    public boolean isInUse(String ownerEmail, Long categoryId) {
        return dividaRepository.existsByCategoria_IdAndUserEmailIgnoreCase(categoryId, ownerEmail);
    }

    private DebtCategory toDomain(CategoriaDivida entity) {
        return new DebtCategory(entity.getId(), entity.getNome(), entity.getUserEmail());
    }
}
