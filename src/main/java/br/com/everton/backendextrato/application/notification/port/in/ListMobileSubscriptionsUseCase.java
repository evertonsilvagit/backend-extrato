package br.com.everton.backendextrato.application.notification.port.in;

import br.com.everton.backendextrato.domain.notification.MobileSubscription;

import java.util.List;

public interface ListMobileSubscriptionsUseCase {
    List<MobileSubscription> execute(String userEmail);
}
