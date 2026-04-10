package br.com.everton.backendextrato.application.notification.port.in;

public interface RemoveMobileSubscriptionUseCase {
    void execute(String expoPushToken);
}
