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
    private static final List<String> CATEGORIAS_RECEBIMENTO_VALIDAS = List.of(
            "STANDARD",
            "SALARY_ADVANCE",
            "SALARY_SETTLEMENT"
    );

    private final EntradaRepository entradaRepository;

    public EntradaService(EntradaRepository entradaRepository) {
        this.entradaRepository = entradaRepository;
    }

    @Transactional
    public EntradaDto criar(String userEmail, CreateEntradaRequest req) {
        validar(req);

        Entrada entrada;
        if (req.id() != null) {
            entrada = entradaRepository.findByIdAndUserEmailIgnoreCase(req.id(), userEmail)
                    .orElseThrow(() -> new IllegalArgumentException("Entrada nao encontrada para o usuario autenticado."));
        } else {
            entrada = new Entrada();
        }

        entrada.setNome(req.nome());
        entrada.setTipo(req.tipo());
        entrada.setValor(req.valor());
        entrada.setTaxaImposto(req.taxaImposto());
        entrada.setDiasRecebimento(req.diasRecebimento());
        entrada.setValorLiquido(Boolean.TRUE.equals(req.valorLiquido()));
        entrada.setCategoriaRecebimento(normalizeCategoriaRecebimento(req.categoriaRecebimento()));
        entrada.setOrdem(resolveOrder(userEmail, entrada, req.ordem()));

        List<EntradaMes> meses = req.mesesVigencia().stream().map(month -> {
            EntradaMes entryMonth = new EntradaMes();
            entryMonth.setEntrada(entrada);
            entryMonth.setMes(month);
            return entryMonth;
        }).collect(Collectors.toList());

        entrada.getMeses().clear();
        entrada.getMeses().addAll(meses);
        entrada.setUserEmail(userEmail);

        Entrada saved = entradaRepository.save(entrada);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public Optional<EntradaDto> buscarPorId(String userEmail, Long id) {
        return entradaRepository.findByIdAndUserEmailIgnoreCase(id, userEmail).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<EntradaDto> listar(String userEmail, Integer page, Integer size) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size <= 0 ? 20 : size;
        Page<Entrada> pageResult = entradaRepository.findAllByUserEmailIgnoreCaseOrderByOrdemAscNomeAscIdAsc(userEmail, PageRequest.of(safePage, safeSize));
        return pageResult.getContent().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public boolean remover(String userEmail, Long id) {
        Optional<Entrada> entrada = entradaRepository.findByIdAndUserEmailIgnoreCase(id, userEmail);
        if (entrada.isEmpty()) {
            return false;
        }

        entradaRepository.delete(entrada.get());
        return true;
    }

    private EntradaDto toDto(Entrada entrada) {
        List<Integer> months = entrada.getMeses().stream().map(EntradaMes::getMes).collect(Collectors.toList());
        return new EntradaDto(
                entrada.getId(),
                entrada.getNome(),
                entrada.getTipo(),
                entrada.getValor(),
                entrada.getTaxaImposto(),
                entrada.getDiasRecebimento(),
                Boolean.TRUE.equals(entrada.getValorLiquido()),
                normalizeCategoriaRecebimento(entrada.getCategoriaRecebimento()),
                months,
                entrada.getOrdem()
        );
    }

    private Integer resolveOrder(String userEmail, Entrada entrada, Integer requestedOrder) {
        if (requestedOrder != null) {
            return requestedOrder;
        }

        if (entrada.getOrdem() != null) {
            return entrada.getOrdem();
        }

        return entradaRepository.findTopByUserEmailIgnoreCaseOrderByOrdemDescIdDesc(userEmail)
                .map(existing -> existing.getOrdem() + 1)
                .orElse(1);
    }

    private void validar(CreateEntradaRequest req) {
        if (req == null) {
            throw new IllegalArgumentException("payload obrigatorio");
        }
        if (req.nome() == null || req.nome().isBlank()) {
            throw new IllegalArgumentException("nome e obrigatorio");
        }
        if (req.tipo() == null || req.tipo().isBlank()) {
            throw new IllegalArgumentException("tipo e obrigatorio");
        }
        if (req.valor() == null || req.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("valor deve ser > 0");
        }
        if (req.taxaImposto() == null || req.taxaImposto().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("taxaImposto deve ser >= 0");
        }
        if (req.diasRecebimento() != null) {
            boolean validDays = req.diasRecebimento().stream().allMatch(day -> day != null && day >= 1 && day <= 31);
            if (!validDays) {
                throw new IllegalArgumentException("diasRecebimento deve conter valores entre 1 e 31");
            }
        }
        String categoriaRecebimento = normalizeCategoriaRecebimento(req.categoriaRecebimento());
        if (!CATEGORIAS_RECEBIMENTO_VALIDAS.contains(categoriaRecebimento)) {
            throw new IllegalArgumentException("categoriaRecebimento invalida");
        }
        if (req.mesesVigencia() == null || req.mesesVigencia().isEmpty()) {
            throw new IllegalArgumentException("mesesVigencia e obrigatorio");
        }
        boolean validMonths = req.mesesVigencia().stream().allMatch(month -> month != null && month >= 1 && month <= 12);
        if (!validMonths) {
            throw new IllegalArgumentException("mesesVigencia deve conter valores entre 1 e 12");
        }
        if (req.ordem() != null && req.ordem() < 1) {
            throw new IllegalArgumentException("ordem deve ser >= 1");
        }
    }

    private String normalizeCategoriaRecebimento(String categoriaRecebimento) {
        if (categoriaRecebimento == null || categoriaRecebimento.isBlank()) {
            return "STANDARD";
        }

        return categoriaRecebimento.trim().toUpperCase();
    }
}
