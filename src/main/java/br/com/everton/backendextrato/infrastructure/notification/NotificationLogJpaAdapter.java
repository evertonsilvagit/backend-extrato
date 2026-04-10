package br.com.everton.backendextrato.infrastructure.notification;

import br.com.everton.backendextrato.application.notification.port.out.NotificationLogPort;
import br.com.everton.backendextrato.model.BillPaymentNotificationLog;
import br.com.everton.backendextrato.repository.BillPaymentNotificationLogRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class NotificationLogJpaAdapter implements NotificationLogPort {

    private final BillPaymentNotificationLogRepository repository;

    public NotificationLogJpaAdapter(BillPaymentNotificationLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsForUserAndDate(String userEmail, LocalDate referenceDate) {
        return repository.existsByUserEmailIgnoreCaseAndReferenceDate(userEmail, referenceDate);
    }

    @Override
    public void save(String userEmail, LocalDate referenceDate) {
        BillPaymentNotificationLog logEntry = new BillPaymentNotificationLog();
        logEntry.setUserEmail(userEmail);
        logEntry.setReferenceDate(referenceDate);
        repository.save(logEntry);
    }
}
