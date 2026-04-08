package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.ContaDto;
import br.com.everton.backendextrato.model.CategoriaConta;
import br.com.everton.backendextrato.model.Conta;
import br.com.everton.backendextrato.repository.CategoriaContaRepository;
import br.com.everton.backendextrato.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final CategoriaContaRepository categoriaRepository;

    public ContaService(ContaRepository contaRepository, CategoriaContaRepository categoriaRepository) {
        this.contaRepository = contaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @Transactional
    public ContaDto criar(String userEmail, ContaDto req) {
        validar(req);

        Conta conta;
        Long previousCategoryId = null;
        Integer previousOrder = null;
        if (req.id() != null) {
            conta = contaRepository.findByIdAndUserEmailIgnoreCase(req.id(), userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Conta nao encontrada para o usuario autenticado."));
            previousCategoryId = conta.getCategoria() != null ? conta.getCategoria().getId() : null;
            previousOrder = conta.getOrdem();
        } else {
            conta = new Conta();
        }

        CategoriaConta categoria = categoriaRepository.findByUserEmailIgnoreCaseAndNomeIgnoreCase(userEmail, req.categoria().trim())
                .orElseThrow(() -> new IllegalArgumentException("Categoria de conta invalida para o usuario autenticado."));

        conta.setDescricao(req.descricao().trim());
        conta.setValor(req.valor());
        conta.setDiaPagamento(req.diaPagamento());
        conta.setCategoria(categoria);
        conta.setMesesVigencia(req.mesesVigencia());
        conta.setUserEmail(userEmail);
        conta.setOrdem(resolveOrder(userEmail, categoria.getId(), conta.getId(), req.ordem(), previousCategoryId, previousOrder));

        Conta salvo = contaRepository.save(conta);
        normalizeCategoryOrders(userEmail, categoria.getId(), salvo.getId(), salvo.getOrdem());

        if (previousCategoryId != null && !previousCategoryId.equals(categoria.getId())) {
            normalizeCategoryOrders(userEmail, previousCategoryId, null, null);
        }

        return toDto(salvo);
    }

    @Transactional(readOnly = true)
    public List<ContaDto> listar(String userEmail) {
        return contaRepository.findAllByUserEmailIgnoreCaseOrderByOrdemAscDescricaoAscIdAsc(userEmail).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<ContaDto> buscarPorId(String userEmail, Long id) {
        return contaRepository.findByIdAndUserEmailIgnoreCase(id, userEmail).map(this::toDto);
    }

    @Transactional
    public boolean remover(String userEmail, Long id) {
        Optional<Conta> conta = contaRepository.findByIdAndUserEmailIgnoreCase(id, userEmail);
        if (conta.isEmpty()) {
            return false;
        }

        Long categoryId = conta.get().getCategoria() != null ? conta.get().getCategoria().getId() : null;
        contaRepository.delete(conta.get());
        if (categoryId != null) {
            normalizeCategoryOrders(userEmail, categoryId, null, null);
        }
        return true;
    }

    private ContaDto toDto(Conta conta) {
        return new ContaDto(
                conta.getId(),
                conta.getDescricao(),
                conta.getValor(),
                conta.getDiaPagamento(),
                conta.getCategoria() != null ? conta.getCategoria().getNome() : "Sem categoria",
                conta.getMesesVigencia(),
                conta.getOrdem()
        );
    }

    private Integer resolveOrder(
            String userEmail,
            Long categoriaId,
            Long contaId,
            Integer requestedOrder,
            Long previousCategoryId,
            Integer previousOrder
    ) {
        List<Conta> categoryBills = new ArrayList<>(contaRepository.findAllByUserEmailIgnoreCaseAndCategoria_IdOrderByOrdemAscIdAsc(userEmail, categoriaId));
        categoryBills.removeIf(existing -> contaId != null && contaId.equals(existing.getId()));

        if (requestedOrder != null) {
            return clampOrder(requestedOrder, categoryBills.size() + 1);
        }

        if (previousOrder != null && previousCategoryId != null && previousCategoryId.equals(categoriaId)) {
            return clampOrder(previousOrder, categoryBills.size() + 1);
        }

        return categoryBills.size() + 1;
    }

    private void normalizeCategoryOrders(String userEmail, Long categoriaId, Long prioritizedContaId, Integer prioritizedOrder) {
        List<Conta> categoryBills = new ArrayList<>(contaRepository.findAllByUserEmailIgnoreCaseAndCategoria_IdOrderByOrdemAscIdAsc(userEmail, categoriaId));

        if (prioritizedContaId != null && prioritizedOrder != null) {
            Conta prioritized = null;
            for (Conta existing : categoryBills) {
                if (prioritizedContaId.equals(existing.getId())) {
                    prioritized = existing;
                    break;
                }
            }

            if (prioritized != null) {
                categoryBills.remove(prioritized);
                categoryBills.add(clampOrder(prioritizedOrder, categoryBills.size() + 1) - 1, prioritized);
            }
        }

        boolean changed = false;
        for (int index = 0; index < categoryBills.size(); index++) {
            int expectedOrder = index + 1;
            Conta existing = categoryBills.get(index);
            if (!Integer.valueOf(expectedOrder).equals(existing.getOrdem())) {
                existing.setOrdem(expectedOrder);
                changed = true;
            }
        }

        if (changed) {
            contaRepository.saveAll(categoryBills);
        }
    }

    private Integer clampOrder(Integer requestedOrder, int maxOrder) {
        if (requestedOrder == null) {
            return maxOrder;
        }
        return Math.max(1, Math.min(requestedOrder, maxOrder));
    }

    private void validar(ContaDto req) {
        if (req == null) {
            throw new IllegalArgumentException("payload obrigatorio");
        }
        if (req.descricao() == null || req.descricao().isBlank()) {
            throw new IllegalArgumentException("descricao e obrigatoria");
        }
        if (req.valor() == null || req.valor().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("valor deve ser >= 0");
        }
        if (req.diaPagamento() == null || req.diaPagamento() < 1 || req.diaPagamento() > 31) {
            throw new IllegalArgumentException("diaPagamento deve estar entre 1 e 31");
        }
        if (req.categoria() == null || req.categoria().isBlank()) {
            throw new IllegalArgumentException("categoria e obrigatoria");
        }
        if (req.ordem() != null && req.ordem() < 1) {
            throw new IllegalArgumentException("ordem deve ser >= 1");
        }
    }
}
