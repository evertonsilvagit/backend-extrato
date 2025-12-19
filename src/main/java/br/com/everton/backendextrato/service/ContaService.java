package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.ContaDto;
import br.com.everton.backendextrato.model.Conta;
import br.com.everton.backendextrato.repository.ContaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ContaService {

    private final ContaRepository contaRepository;

    public ContaService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Transactional
    public ContaDto criar(ContaDto req) {
        validar(req);

        Conta conta;
        if (req.id() != null) {
            conta = contaRepository.findById(req.id())
                    .orElse(new Conta());
        } else {
            conta = new Conta();
        }

        conta.setDescricao(req.descricao());
        conta.setValor(req.valor());
        conta.setDiaPagamento(req.diaPagamento());
        conta.setCategoria(req.categoria());
        conta.setMesesVigencia(req.mesesVigencia());

        Conta salvo = contaRepository.save(conta);
        return toDto(salvo);
    }

    @Transactional(readOnly = true)
    public List<ContaDto> listar() {
        return contaRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ContaDto> buscarPorId(Long id) {
        return contaRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public boolean remover(Long id) {
        if (!contaRepository.existsById(id)) return false;
        contaRepository.deleteById(id);
        return true;
    }

    private ContaDto toDto(Conta c) {
        return new ContaDto(
                c.getId(),
                c.getDescricao(),
                c.getValor(),
                c.getDiaPagamento(),
                c.getCategoria(),
                c.getMesesVigencia()
        );
    }

    private void validar(ContaDto req) {
        if (req == null) throw new IllegalArgumentException("payload obrigatório");
        if (req.descricao() == null || req.descricao().isBlank()) throw new IllegalArgumentException("descricao é obrigatória");
        if (req.valor() == null || req.valor().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("valor deve ser >= 0");
        if (req.diaPagamento() == null || req.diaPagamento() < 1 || req.diaPagamento() > 31)
            throw new IllegalArgumentException("diaPagamento deve estar entre 1 e 31");
    }
}
