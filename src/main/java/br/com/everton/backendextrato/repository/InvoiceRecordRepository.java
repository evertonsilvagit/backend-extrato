package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.InvoiceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRecordRepository extends JpaRepository<InvoiceRecord, Long> {
    List<InvoiceRecord> findAllByUserEmailIgnoreCaseOrderByIssueYearDescIssueDateDescIdDesc(String userEmail);
    Optional<InvoiceRecord> findByUserEmailIgnoreCaseAndSourcePath(String userEmail, String sourcePath);
}
