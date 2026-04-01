package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.FinancialSeparationCashboxDto;
import br.com.everton.backendextrato.dto.FinancialSeparationEntryDto;
import br.com.everton.backendextrato.dto.FinancialSeparationWorkspaceDto;
import br.com.everton.backendextrato.model.FinancialSeparationWorkspace;
import br.com.everton.backendextrato.repository.FinancialSeparationWorkspaceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class FinancialSeparationWorkspaceService {

    private final FinancialSeparationWorkspaceRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FinancialSeparationWorkspaceService(FinancialSeparationWorkspaceRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<FinancialSeparationWorkspaceDto> getByUserEmail(String userEmail) {
        return repository.findByUserEmailIgnoreCase(userEmail).map(this::toDto);
    }

    @Transactional
    public FinancialSeparationWorkspaceDto save(String userEmail, FinancialSeparationWorkspaceDto request) {
        if (request == null) {
            throw new IllegalArgumentException("Os dados da central PF / PJ s\u00e3o obrigat\u00f3rios.");
        }

        FinancialSeparationWorkspace workspace = repository.findByUserEmailIgnoreCase(userEmail)
                .orElseGet(FinancialSeparationWorkspace::new);

        workspace.setUserEmail(userEmail);
        workspace.setCashboxesJson(writeJson(request.cashboxes() == null ? List.of() : request.cashboxes()));
        workspace.setEntriesJson(writeJson(request.entries() == null ? List.of() : request.entries()));

        return toDto(repository.save(workspace));
    }

    private FinancialSeparationWorkspaceDto toDto(FinancialSeparationWorkspace workspace) {
        return new FinancialSeparationWorkspaceDto(
                readCashboxes(workspace.getCashboxesJson()),
                readEntries(workspace.getEntriesJson())
        );
    }

    private List<FinancialSeparationCashboxDto> readCashboxes(String json) {
        return readJson(json, new TypeReference<List<FinancialSeparationCashboxDto>>() {});
    }

    private List<FinancialSeparationEntryDto> readEntries(String json) {
        return readJson(json, new TypeReference<List<FinancialSeparationEntryDto>>() {});
    }

    private <T> List<T> readJson(String json, TypeReference<List<T>> typeReference) {
        if (json == null || json.isBlank()) {
            return List.of();
        }

        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("N\u00e3o foi poss\u00edvel ler os dados salvos da central PF / PJ.", ex);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("N\u00e3o foi poss\u00edvel salvar os dados da central PF / PJ.", ex);
        }
    }
}
