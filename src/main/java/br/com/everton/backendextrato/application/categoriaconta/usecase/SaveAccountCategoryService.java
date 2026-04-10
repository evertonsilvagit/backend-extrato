package br.com.everton.backendextrato.application.categoriaconta.usecase;

import br.com.everton.backendextrato.application.categoriaconta.exception.AccountCategoryConflictException;
import br.com.everton.backendextrato.application.categoriaconta.exception.AccountCategoryNotFoundException;
import br.com.everton.backendextrato.application.categoriaconta.exception.AccountCategoryValidationException;
import br.com.everton.backendextrato.application.categoriaconta.port.in.SaveAccountCategoryUseCase;
import br.com.everton.backendextrato.application.categoriaconta.port.out.AccountCategoryRepository;
import br.com.everton.backendextrato.application.categoriaconta.usecase.command.SaveAccountCategoryCommand;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.categoriaconta.AccountCategory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaveAccountCategoryService implements SaveAccountCategoryUseCase {

    private final AccountCategoryRepository accountCategoryRepository;

    public SaveAccountCategoryService(AccountCategoryRepository accountCategoryRepository) {
        this.accountCategoryRepository = accountCategoryRepository;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.ACCOUNT_CATEGORIES, key = "#command.ownerEmail()")
    public AccountCategory execute(SaveAccountCategoryCommand command) {
        validate(command);

        AccountCategory draft = new AccountCategory(command.id(), command.name(), command.ownerEmail());
        AccountCategory category = loadExistingOrCreate(draft);

        accountCategoryRepository.findByOwnerEmailAndName(command.ownerEmail(), draft.name())
                .filter(existing -> !existing.id().equals(category.id()))
                .ifPresent(existing -> {
                    throw new AccountCategoryConflictException("Já existe uma categoria de conta com este nome.");
                });

        return accountCategoryRepository.save(category);
    }

    private AccountCategory loadExistingOrCreate(AccountCategory draft) {
        if (draft.id() == null) {
            return draft;
        }

        return accountCategoryRepository.findByIdAndOwnerEmail(draft.id(), draft.ownerEmail())
                .map(existing -> new AccountCategory(existing.id(), draft.name(), existing.ownerEmail()))
                .orElseThrow(() -> new AccountCategoryNotFoundException("Categoria de conta não encontrada para o usuário autenticado."));
    }

    private void validate(SaveAccountCategoryCommand command) {
        if (command == null) {
            throw new AccountCategoryValidationException("Payload obrigatório.");
        }
    }
}
