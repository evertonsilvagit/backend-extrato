package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.CreateLancamentoRequest;
import br.com.everton.backendextrato.dto.ExtratoResponse;
import br.com.everton.backendextrato.dto.LancamentoDto;
import br.com.everton.backendextrato.model.Tipo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class ExtratoService {

    private final Map<Long, LancamentoDto> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public LancamentoDto criarLancamento(CreateLancamentoRequest req) {
        Tipo tipo = Tipo.valueOf(req.tipo().toUpperCase());
        Long id = seq.getAndIncrement();
        LancamentoDto dto = new LancamentoDto(
                id,
                req.data(),
                tipo,
                req.valor(),
                req.descricao(),
                req.categoria(),
                req.contaId()
        );
        store.put(id, dto);
        return dto;
    }

    public Optional<LancamentoDto> buscarPorId(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean remover(Long id) {
        return store.remove(id) != null;
    }

    public ExtratoResponse listarExtrato(Long contaId, LocalDate de, LocalDate ate, Integer page, Integer size) {
        if (contaId == null) throw new IllegalArgumentException("contaId é obrigatório");
        LocalDate deEfetivo = de;
        LocalDate ateEfetivo = ate;

        List<LancamentoDto> daConta = store.values().stream()
                .filter(l -> contaId.equals(l.contaId()))
                .sorted(Comparator.comparing(LancamentoDto::data).thenComparing(LancamentoDto::id))
                .collect(Collectors.toList());

        // Filtrados por período (inclusive)
        List<LancamentoDto> noPeriodo = daConta.stream()
                .filter(l -> (deEfetivo == null || !l.data().isBefore(deEfetivo)))
                .filter(l -> (ateEfetivo == null || !l.data().isAfter(ateEfetivo)))
                .collect(Collectors.toList());

        // Paginação simples
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size <= 0 ? 20 : size;
        int from = Math.min(p * s, noPeriodo.size());
        int to = Math.min(from + s, noPeriodo.size());
        List<LancamentoDto> pageItems = new ArrayList<>(noPeriodo.subList(from, to));

        // Saldo anterior: soma dos lançamentos antes de "de"
        BigDecimal saldoAnterior = daConta.stream()
                .filter(l -> deEfetivo != null && l.data().isBefore(deEfetivo))
                .map(this::delta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Saldo atual: saldoAnterior + lançamentos do período
        BigDecimal deltaPeriodo = noPeriodo.stream()
                .map(this::delta)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal saldoAtual = saldoAnterior.add(deltaPeriodo);

        return new ExtratoResponse(
                contaId,
                deEfetivo,
                ateEfetivo,
                saldoAnterior,
                saldoAtual,
                pageItems
        );
    }

    private BigDecimal delta(LancamentoDto l) {
        return l.tipo() == Tipo.CREDITO ? l.valor() : l.valor().negate();
    }
}
