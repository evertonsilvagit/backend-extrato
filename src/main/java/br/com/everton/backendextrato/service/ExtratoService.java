package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.CreateLancamentoRequest;
import br.com.everton.backendextrato.dto.ExtratoResponse;
import br.com.everton.backendextrato.dto.LancamentoDto;
import br.com.everton.backendextrato.model.Lancamento;
import br.com.everton.backendextrato.model.Tipo;
import br.com.everton.backendextrato.repository.LancamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExtratoService {

    private final LancamentoRepository lancamentoRepository;

    public ExtratoService(LancamentoRepository lancamentoRepository) {
        this.lancamentoRepository = lancamentoRepository;
    }

    @Transactional
    public LancamentoDto criarLancamento(CreateLancamentoRequest req) {
        Tipo tipo = Tipo.valueOf(req.tipo().toUpperCase());
        Lancamento l = new Lancamento();
        l.setData(req.data());
        l.setTipo(tipo);
        l.setValor(req.valor());
        l.setDescricao(req.descricao());
        l.setCategoria(req.categoria());
        l.setContaId(req.contaId());
        Lancamento salvo = lancamentoRepository.save(l);
        return toDto(salvo);
    }

    @Transactional(readOnly = true)
    public Optional<LancamentoDto> buscarPorId(Long id) {
        return lancamentoRepository.findById(id).map(this::toDto);
    }

    @Transactional
    public boolean remover(Long id) {
        if (!lancamentoRepository.existsById(id)) return false;
        lancamentoRepository.deleteById(id);
        return true;
    }

    @Transactional(readOnly = true)
    public ExtratoResponse listarExtrato(Long contaId, LocalDate de, LocalDate ate, Integer page, Integer size) {
        if (contaId == null) throw new IllegalArgumentException("contaId é obrigatório");
        LocalDate deEfetivo = de;
        LocalDate ateEfetivo = ate;

        List<Lancamento> noPeriodo;
        if (deEfetivo != null && ateEfetivo != null) {
            noPeriodo = lancamentoRepository.findByContaIdAndDataBetweenOrderByDataAscIdAsc(contaId, deEfetivo, ateEfetivo);
        } else {
            noPeriodo = lancamentoRepository.findByContaIdOrderByDataAscIdAsc(contaId);
            if (deEfetivo != null) {
                noPeriodo = noPeriodo.stream().filter(l -> !l.getData().isBefore(deEfetivo)).collect(Collectors.toList());
            }
            if (ateEfetivo != null) {
                noPeriodo = noPeriodo.stream().filter(l -> !l.getData().isAfter(ateEfetivo)).collect(Collectors.toList());
            }
        }

        // Paginação simples
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size <= 0 ? 20 : size;
        int from = Math.min(p * s, noPeriodo.size());
        int to = Math.min(from + s, noPeriodo.size());
        List<Lancamento> pageItems = new ArrayList<>(noPeriodo.subList(from, to));

        // Saldo anterior
        BigDecimal saldoAnterior = BigDecimal.ZERO;
        if (deEfetivo != null) {
            List<Lancamento> anteriores = lancamentoRepository.findByContaIdAndDataBeforeOrderByDataAscIdAsc(contaId, deEfetivo);
            saldoAnterior = anteriores.stream().map(this::delta).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        BigDecimal deltaPeriodo = pageItems.stream().map(this::delta).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldoAtual = saldoAnterior.add(deltaPeriodo);

        List<LancamentoDto> itensDto = pageItems.stream().map(this::toDto).collect(Collectors.toList());

        return new ExtratoResponse(
                contaId,
                deEfetivo,
                ateEfetivo,
                saldoAnterior,
                saldoAtual,
                itensDto
        );
    }

    private BigDecimal delta(Lancamento l) {
        return l.getTipo() == Tipo.CREDITO ? l.getValor() : l.getValor().negate();
    }

    private LancamentoDto toDto(Lancamento l) {
        return new LancamentoDto(
                l.getId(),
                l.getData(),
                l.getTipo(),
                l.getValor(),
                l.getDescricao(),
                l.getCategoria(),
                l.getContaId()
        );
    }
}
