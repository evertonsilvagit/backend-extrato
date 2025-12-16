package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.CreateEntradaRequest;
import br.com.everton.backendextrato.dto.EntradaDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EntradaService {

    private final Map<Long, EntradaDto> store = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(1);

    public EntradaDto criar(CreateEntradaRequest req) {
        validar(req);
        Long id = seq.getAndIncrement();
        EntradaDto dto = new EntradaDto(
                id,
                req.nome(),
                req.tipo(),
                req.valor(),
                req.taxaImposto(),
                req.mesesVigencia()
        );
        store.put(id, dto);
        return dto;
    }

    public Optional<EntradaDto> buscarPorId(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public List<EntradaDto> listar(Integer page, Integer size) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size <= 0 ? 20 : size;
        ArrayList<EntradaDto> all = new ArrayList<>(store.values());
        int from = Math.min(p * s, all.size());
        int to = Math.min(from + s, all.size());
        return all.subList(from, to);
    }

    public boolean remover(Long id) {
        return store.remove(id) != null;
    }

    private void validar(CreateEntradaRequest req) {
        if (req == null) throw new IllegalArgumentException("payload obrigatório");
        if (req.nome() == null || req.nome().isBlank()) throw new IllegalArgumentException("nome é obrigatório");
        if (req.tipo() == null || req.tipo().isBlank()) throw new IllegalArgumentException("tipo é obrigatório");
        if (req.valor() == null || req.valor().compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("valor deve ser > 0");
        if (req.taxaImposto() == null || req.taxaImposto().compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("taxaImposto deve ser >= 0");
        if (req.mesesVigencia() == null || req.mesesVigencia().isEmpty())
            throw new IllegalArgumentException("mesesVigencia é obrigatório");
        boolean mesesOk = req.mesesVigencia().stream().allMatch(m -> m != null && m >= 1 && m <= 12);
        if (!mesesOk) throw new IllegalArgumentException("mesesVigencia deve conter valores entre 1 e 12");
    }
}
