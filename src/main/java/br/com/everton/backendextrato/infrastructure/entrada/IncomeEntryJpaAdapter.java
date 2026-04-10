package br.com.everton.backendextrato.infrastructure.entrada;

import br.com.everton.backendextrato.application.entrada.exception.IncomeEntryValidationException;
import br.com.everton.backendextrato.application.entrada.port.out.IncomeEntryRepository;
import br.com.everton.backendextrato.domain.entrada.IncomeEntry;
import br.com.everton.backendextrato.model.Entrada;
import br.com.everton.backendextrato.model.EntradaMes;
import br.com.everton.backendextrato.repository.EntradaRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class IncomeEntryJpaAdapter implements IncomeEntryRepository {

    private final EntradaRepository entradaRepository;

    public IncomeEntryJpaAdapter(EntradaRepository entradaRepository) {
        this.entradaRepository = entradaRepository;
    }

    @Override
    public Optional<IncomeEntry> findByIdAndOwnerEmail(Long id, String ownerEmail) {
        return entradaRepository.findByIdAndUserEmailIgnoreCase(id, ownerEmail).map(this::toDomain);
    }

    @Override
    public List<IncomeEntry> findAllByOwnerEmail(String ownerEmail, int page, int size) {
        return entradaRepository.findAllByUserEmailIgnoreCaseOrderByOrdemAscNomeAscIdAsc(ownerEmail, PageRequest.of(page, size))
                .getContent()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<IncomeEntry> findTopByOwnerEmailOrderBySortOrderDesc(String ownerEmail) {
        return entradaRepository.findTopByUserEmailIgnoreCaseOrderByOrdemDescIdDesc(ownerEmail).map(this::toDomain);
    }

    @Override
    public IncomeEntry save(IncomeEntry incomeEntry) {
        Entrada entity = incomeEntry.id() != null ? entradaRepository.findById(incomeEntry.id()).orElseGet(Entrada::new) : new Entrada();

        entity.setNome(incomeEntry.name());
        entity.setTipo(incomeEntry.type());
        entity.setValor(incomeEntry.amount());
        entity.setTaxaImposto(incomeEntry.taxRate());
        entity.setDiasRecebimento(incomeEntry.paymentDays());
        entity.setValorLiquido(Boolean.TRUE.equals(incomeEntry.netAmount()));
        entity.setCategoriaRecebimento(incomeEntry.incomeCategory());
        entity.setOrdem(incomeEntry.sortOrder());
        entity.setUserEmail(incomeEntry.ownerEmail());

        List<EntradaMes> months = incomeEntry.activeMonths().stream().map(month -> {
            if (month == null) {
                throw new IncomeEntryValidationException("mesesVigencia deve conter valores entre 1 e 12");
            }
            EntradaMes entryMonth = new EntradaMes();
            entryMonth.setEntrada(entity);
            entryMonth.setMes(month);
            return entryMonth;
        }).toList();

        entity.getMeses().clear();
        entity.getMeses().addAll(months);

        return toDomain(entradaRepository.save(entity));
    }

    @Override
    public void delete(IncomeEntry incomeEntry) {
        entradaRepository.findById(incomeEntry.id()).ifPresent(entradaRepository::delete);
    }

    private IncomeEntry toDomain(Entrada entity) {
        return new IncomeEntry(
                entity.getId(),
                entity.getNome(),
                entity.getTipo(),
                entity.getValor(),
                entity.getTaxaImposto(),
                entity.getDiasRecebimento(),
                Boolean.TRUE.equals(entity.getValorLiquido()),
                entity.getCategoriaRecebimento(),
                entity.getMeses().stream().map(EntradaMes::getMes).toList(),
                entity.getOrdem(),
                entity.getUserEmail()
        );
    }
}
