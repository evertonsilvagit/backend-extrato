package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.CreateEntradaRequest;
import br.com.everton.backendextrato.dto.EntradaDto;
import br.com.everton.backendextrato.model.Entrada;
import br.com.everton.backendextrato.model.EntradaMes;
import br.com.everton.backendextrato.repository.EntradaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EntradaService {

    private final EntradaRepository entradaRepository;

    public EntradaService(EntradaRepository entradaRepository) {
        this.entradaRepository = entradaRepository;
    }

    @Transactional
    public EntradaDto criar(String userEmail, CreateEntradaRequest req) {
        validar(req);

        Entrada ent = new Entrada();
        ent.setNome(req.nome());
        ent.setTipo(req.tipo());
        ent.setValor(req.valor());
        ent.setTaxaImposto(req.taxaImposto());

        // meses de vigência
        List<EntradaMes> meses = req.mesesVigencia().stream().map(m -> {
            EntradaMes em = new EntradaMes();
            em.setEntrada(ent);
            em.setMes(m);
            return em;
        }).collect(Collectors.toList());
        ent.setMeses(meses);
        ent.setUserEmail(userEmail);

        Entrada salvo = entradaRepository.save(ent);
        return toDto(salvo);
    }

    @Transactional(readOnly = true)
    public Optional<EntradaDto> buscarPorId(String userEmail, Long id) {
        return entradaRepository.findByIdAndUserEmailIgnoreCase(id, userEmail).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<EntradaDto> listar(String userEmail, Integer page, Integer size) {
        int p = page == null || page < 0 ? 0 : page;
        int s = size == null || size <= 0 ? 20 : size;
        Page<Entrada> pagina = entradaRepository.findAllByUserEmailIgnoreCase(userEmail, PageRequest.of(p, s));
        return pagina.getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public boolean remover(String userEmail, Long id) {
        Optional<Entrada> entrada = entradaRepository.findByIdAndUserEmailIgnoreCase(id, userEmail);
        if (entrada.isEmpty()) return false;
        entradaRepository.delete(entrada.get());
        return true;
    }

    private EntradaDto toDto(Entrada e) {
        List<Integer> meses = e.getMeses().stream().map(EntradaMes::getMes).collect(Collectors.toList());
        return new EntradaDto(
                e.getId(),
                e.getNome(),
                e.getTipo(),
                e.getValor(),
                e.getTaxaImposto(),
                meses
        );
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
