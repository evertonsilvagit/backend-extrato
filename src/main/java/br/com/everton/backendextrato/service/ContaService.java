package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.ContaDto;
import br.com.everton.backendextrato.model.CategoriaConta;
import br.com.everton.backendextrato.model.Conta;
import br.com.everton.backendextrato.repository.CategoriaContaRepository;
import br.com.everton.backendextrato.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        if (req.id() != null) {
            conta = contaRepository.findByIdAndUserEmailIgnoreCase(req.id(), userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Conta nao encontrada para o usuario autenticado."));
        } else {
            conta = new Conta();
        }

        CategoriaConta categoria = categoriaRepository.findByUserEmailIgnoreCaseAndNomeIgnoreCase(userEmail, req.categoria().trim())
                .orElseThrow(() -> new IllegalArgumentException("Categoria de conta invalida para o usuario autenticado."));

        conta.setDescricao(req.descricao().trim());
        conta.setValor(req.valor());
        conta.setDiaPagamento(req.diaPagamento());
        conta.setOrdem(resolveOrder(userEmail, conta, req.ordem()));
        conta.setCategoria(categoria);
        conta.setMesesVigencia(req.mesesVigencia());
        conta.setUserEmail(userEmail);

        Conta salvo = contaRepository.save(conta);
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

        contaRepository.delete(conta.get());
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

    private Integer resolveOrder(String userEmail, Conta conta, Integer requestedOrder) {
        if (requestedOrder != null) {
            return requestedOrder;
        }

        if (conta.getOrdem() != null) {
            return conta.getOrdem();
        }

        return contaRepository.findTopByUserEmailIgnoreCaseOrderByOrdemDescIdDesc(userEmail)
                .map(existing -> existing.getOrdem() + 1)
                .orElse(1);
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
