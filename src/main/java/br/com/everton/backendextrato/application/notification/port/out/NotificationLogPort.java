package br.com.everton.backendextrato.application.notification.port.out;

import java.time.LocalDate;

public interface NotificationLogPort {
    boolean existsForUserAndDate(String userEmail, LocalDate referenceDate);
    void save(String userEmail, LocalDate referenceDate);
}
