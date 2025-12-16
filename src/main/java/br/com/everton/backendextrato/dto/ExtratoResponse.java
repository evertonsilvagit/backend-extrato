package br.com.everton.backendextrato.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExtratoResponse(
        Long contaId,
        LocalDate periodoDe,
        LocalDate periodoAte,
        BigDecimal saldoAnterior,
        BigDecimal saldoAtual,
        List<LancamentoDto> itens
) {}
