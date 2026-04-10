package br.com.everton.backendextrato.service;

import br.com.everton.backendextrato.dto.InvoiceImportResponse;
import br.com.everton.backendextrato.dto.InvoiceRecordDto;
import br.com.everton.backendextrato.model.InvoiceRecord;
import br.com.everton.backendextrato.repository.InvoiceRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InvoiceRecordService {

    private final InvoiceRecordRepository repository;
    private final MongoImportHistoryService importHistoryService;

    public InvoiceRecordService(InvoiceRecordRepository repository, MongoImportHistoryService importHistoryService) {
        this.repository = repository;
        this.importHistoryService = importHistoryService;
    }

    @Transactional(readOnly = true)
    public List<InvoiceRecordDto> list(String userEmail) {
        return repository.findAllByUserEmailIgnoreCaseOrderByIssueYearDescIssueDateDescIdDesc(userEmail)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public InvoiceImportResponse importRecords(String userEmail, List<InvoiceRecordDto> records) {
        if (records == null || records.isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos uma nota fiscal para importação.");
        }

        long start = System.nanoTime();
        int created = 0;
        int updated = 0;
        Map<String, InvoiceRecord> recordsBySourcePath = new HashMap<>();

        for (InvoiceRecordDto dto : records) {
            if (dto == null || dto.sourcePath() == null || dto.sourcePath().isBlank()) {
                continue;
            }

            String sourcePath = dto.sourcePath().trim();
            InvoiceRecord record = recordsBySourcePath.get(sourcePath);
            if (record == null) {
                record = repository.findByUserEmailIgnoreCaseAndSourcePath(userEmail, sourcePath)
                        .orElseGet(InvoiceRecord::new);
            }
            boolean isNew = record.getId() == null;
            record.setUserEmail(userEmail);
            record.setSourcePath(sourcePath);
            record.setFilename(required(dto.filename(), "filename"));
            record.setIssueYear(trim(dto.year()));
            record.setMonthFolder(trim(dto.monthFolder()));
            record.setIssueDate(trim(dto.issueDate()));
            record.setInvoiceNumber(trim(dto.number()));
            record.setCustomerDocument(trim(dto.customerDocument()));
            record.setCustomerName(trim(dto.customerName()));
            record.setCustomerEmail(trim(dto.customerEmail()));
            record.setCustomerCity(trim(dto.customerCity()));
            record.setGrossAmount(dto.grossAmount());
            record.setIssAmount(dto.issAmount());
            record.setNetAmount(dto.netAmount());
            record.setServiceType(trim(dto.serviceType()));
            record.setNotes(trim(dto.notes()));
            record.setCanceled(Boolean.TRUE.equals(dto.canceled()));
            record.setRelativePath(trim(dto.relativePath()));

            record = repository.save(record);
            recordsBySourcePath.put(sourcePath, record);
            if (isNew) created++;
            else updated++;
        }

        InvoiceImportResponse response = new InvoiceImportResponse(created, updated, created + updated);

        long durationMs = (System.nanoTime() - start) / 1_000_000L;
        importHistoryService.record(
                userEmail,
                "invoice-records",
                "invoice-records",
                records.size(),
                created,
                updated,
                Math.max(0, records.size() - (created + updated)),
                durationMs,
                Map.of("payloadCount", records.size())
        );

        return response;
    }

    private InvoiceRecordDto toDto(InvoiceRecord record) {
        return new InvoiceRecordDto(
                record.getSourcePath(),
                record.getFilename(),
                record.getIssueYear(),
                record.getMonthFolder(),
                record.getIssueDate(),
                record.getInvoiceNumber(),
                record.getCustomerDocument(),
                record.getCustomerName(),
                record.getCustomerEmail(),
                record.getCustomerCity(),
                record.getGrossAmount(),
                record.getIssAmount(),
                record.getNetAmount(),
                record.getServiceType(),
                record.getNotes(),
                record.isCanceled(),
                record.getRelativePath()
        );
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String required(String value, String field) {
        String trimmed = trim(value);
        if (trimmed == null) {
            throw new IllegalArgumentException("Campo obrigatório ausente na importação: " + field);
        }
        return trimmed;
    }
}
