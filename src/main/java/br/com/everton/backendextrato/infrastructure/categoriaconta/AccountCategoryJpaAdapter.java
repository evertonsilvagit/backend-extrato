package br.com.everton.backendextrato.infrastructure.categoriaconta;

import br.com.everton.backendextrato.application.categoriaconta.port.out.AccountCategoryRepository;
import br.com.everton.backendextrato.application.categoriaconta.port.out.AccountCategoryUsageRepository;
import br.com.everton.backendextrato.domain.categoriaconta.AccountCategory;
import br.com.everton.backendextrato.model.CategoriaConta;
import br.com.everton.backendextrato.repository.CategoriaContaRepository;
import br.com.everton.backendextrato.repository.ContaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AccountCategoryJpaAdapter implements AccountCategoryRepository, AccountCategoryUsageRepository {

    private final CategoriaContaRepository categoriaContaRepository;
    private final ContaRepository contaRepository;

    public AccountCategoryJpaAdapter(
            CategoriaContaRepository categoriaContaRepository,
            ContaRepository contaRepository
    ) {
        this.categoriaContaRepository = categoriaContaRepository;
        this.contaRepository = contaRepository;
    }

    @Override
    public List<AccountCategory> findAllByOwnerEmail(String ownerEmail) {
        return categoriaContaRepository.findAllByUserEmailIgnoreCaseOrderByNomeAsc(ownerEmail).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<AccountCategory> findByIdAndOwnerEmail(Long id, String ownerEmail) {
        return categoriaContaRepository.findByIdAndUserEmailIgnoreCase(id, ownerEmail)
                .map(this::toDomain);
    }

    @Override
    public Optional<AccountCategory> findByOwnerEmailAndName(String ownerEmail, String name) {
        return categoriaContaRepository.findByUserEmailIgnoreCaseAndNomeIgnoreCase(ownerEmail, name)
                .map(this::toDomain);
    }

    @Override
    public AccountCategory save(AccountCategory category) {
        CategoriaConta entity;
        if (category.id() != null) {
            entity = categoriaContaRepository.findById(category.id()).orElseGet(CategoriaConta::new);
        } else {
            entity = new CategoriaConta();
        }

        entity.setNome(category.name());
        entity.setUserEmail(category.ownerEmail());

        return toDomain(categoriaContaRepository.save(entity));
    }

    @Override
    public void delete(AccountCategory category) {
        categoriaContaRepository.findById(category.id())
                .ifPresent(categoriaContaRepository::delete);
    }

    @Override
    public boolean isInUse(String ownerEmail, Long categoryId) {
        return contaRepository.existsByCategoria_IdAndUserEmailIgnoreCase(categoryId, ownerEmail);
    }

    private AccountCategory toDomain(CategoriaConta entity) {
        return new AccountCategory(entity.getId(), entity.getNome(), entity.getUserEmail());
    }
}
