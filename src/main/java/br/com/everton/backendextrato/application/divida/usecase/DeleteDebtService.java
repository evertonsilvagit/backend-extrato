package br.com.everton.backendextrato.application.divida.usecase;

import br.com.everton.backendextrato.application.divida.port.in.DeleteDebtUseCase;
import br.com.everton.backendextrato.application.divida.port.out.DebtRepository;
import br.com.everton.backendextrato.config.CacheNames;
import br.com.everton.backendextrato.domain.divida.Debt;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteDebtService implements DeleteDebtUseCase {

    private final DebtRepository debtRepository;

    public DeleteDebtService(DebtRepository debtRepository) {
        this.debtRepository = debtRepository;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheNames.DEBTS, key = "#ownerEmail")
    public boolean execute(String ownerEmail, Long debtId) {
        Debt debt = debtRepository.findByIdAndOwnerEmail(debtId, ownerEmail).orElse(null);
        if (debt == null) {
            return false;
        }

        debtRepository.delete(debt);
        return true;
    }
}
