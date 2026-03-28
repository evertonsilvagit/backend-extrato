package br.com.everton.backendextrato.repository;

import br.com.everton.backendextrato.model.BillPaymentNotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BillPaymentNotificationLogRepository extends JpaRepository<BillPaymentNotificationLog, Long> {
    boolean existsByUserEmailIgnoreCaseAndReferenceDate(String userEmail, LocalDate referenceDate);
}
