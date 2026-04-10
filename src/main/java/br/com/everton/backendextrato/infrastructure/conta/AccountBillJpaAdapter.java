package br.com.everton.backendextrato.infrastructure.conta;

import br.com.everton.backendextrato.application.conta.exception.AccountBillValidationException;
import br.com.everton.backendextrato.application.conta.port.out.AccountBillRepository;
import br.com.everton.backendextrato.domain.conta.AccountBill;
import br.com.everton.backendextrato.model.CategoriaConta;
import br.com.everton.backendextrato.model.Conta;
import br.com.everton.backendextrato.repository.CategoriaContaRepository;
import br.com.everton.backendextrato.repository.ContaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AccountBillJpaAdapter implements AccountBillRepository {

    private final ContaRepository contaRepository;
    private final CategoriaContaRepository categoriaContaRepository;

    public AccountBillJpaAdapter(
            ContaRepository contaRepository,
            CategoriaContaRepository categoriaContaRepository
    ) {
        this.contaRepository = contaRepository;
        this.categoriaContaRepository = categoriaContaRepository;
    }

    @Override
    public Optional<AccountBill> findByIdAndOwnerEmail(Long id, String ownerEmail) {
        return contaRepository.findByIdAndUserEmailIgnoreCase(id, ownerEmail)
                .map(this::toDomain);
    }

    @Override
    public List<AccountBill> findAllByOwnerEmail(String ownerEmail) {
        return contaRepository.findAllByUserEmailIgnoreCaseOrderByOrdemAscDescricaoAscIdAsc(ownerEmail).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<AccountBill> findAllByOwnerEmailAndCategoryId(String ownerEmail, Long categoryId) {
        return contaRepository.findAllByUserEmailIgnoreCaseAndCategoria_IdOrderByOrdemAscIdAsc(ownerEmail, categoryId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public AccountBill save(AccountBill bill) {
        Conta entity;
        if (bill.id() != null) {
            entity = contaRepository.findById(bill.id()).orElseGet(Conta::new);
        } else {
            entity = new Conta();
        }

        CategoriaConta category = categoriaContaRepository.findById(bill.categoryId())
                .orElseThrow(() -> new AccountBillValidationException("Categoria de conta invalida para o usuario autenticado."));

        entity.setDescricao(bill.description());
        entity.setValor(bill.amount());
        entity.setDiaPagamento(bill.paymentDay());
        entity.setCategoria(category);
        entity.setMesesVigencia(bill.activeMonths());
        entity.setUserEmail(bill.ownerEmail());
        entity.setOrdem(bill.sortOrder());

        return toDomain(contaRepository.save(entity));
    }

    @Override
    public void saveAll(List<AccountBill> bills) {
        List<Conta> entities = bills.stream()
                .map(this::toExistingEntity)
                .toList();
        contaRepository.saveAll(entities);
    }

    @Override
    public void delete(AccountBill bill) {
        contaRepository.findById(bill.id())
                .ifPresent(contaRepository::delete);
    }

    private Conta toExistingEntity(AccountBill bill) {
        Conta entity = contaRepository.findById(bill.id())
                .orElseThrow(() -> new AccountBillValidationException("Conta nao encontrada para o usuario autenticado."));

        CategoriaConta category = categoriaContaRepository.findById(bill.categoryId())
                .orElseThrow(() -> new AccountBillValidationException("Categoria de conta invalida para o usuario autenticado."));

        entity.setDescricao(bill.description());
        entity.setValor(bill.amount());
        entity.setDiaPagamento(bill.paymentDay());
        entity.setCategoria(category);
        entity.setMesesVigencia(bill.activeMonths());
        entity.setUserEmail(bill.ownerEmail());
        entity.setOrdem(bill.sortOrder());
        return entity;
    }

    private AccountBill toDomain(Conta entity) {
        return new AccountBill(
                entity.getId(),
                entity.getDescricao(),
                entity.getValor(),
                entity.getDiaPagamento(),
                entity.getCategoria() != null ? entity.getCategoria().getId() : null,
                entity.getCategoria() != null ? entity.getCategoria().getNome() : "Sem categoria",
                entity.getMesesVigencia(),
                entity.getUserEmail(),
                entity.getOrdem()
        );
    }
}
