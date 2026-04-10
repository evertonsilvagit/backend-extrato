package br.com.everton.backendextrato.application.notification.port.in;

import br.com.everton.backendextrato.domain.notification.WebPushSubscription;

import java.util.List;

public interface ListWebSubscriptionsUseCase {
    List<WebPushSubscription> execute(String userEmail);
}
