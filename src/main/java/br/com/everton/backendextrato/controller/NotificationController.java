package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
import br.com.everton.backendextrato.application.notification.port.in.GetNotificationStatusUseCase;
import br.com.everton.backendextrato.application.notification.port.in.ListMobileSubscriptionsUseCase;
import br.com.everton.backendextrato.application.notification.port.in.ListWebSubscriptionsUseCase;
import br.com.everton.backendextrato.application.notification.port.in.RegisterMobileSubscriptionUseCase;
import br.com.everton.backendextrato.application.notification.port.in.RegisterWebSubscriptionUseCase;
import br.com.everton.backendextrato.application.notification.port.in.RemoveMobileSubscriptionUseCase;
import br.com.everton.backendextrato.application.notification.port.in.RemoveWebSubscriptionUseCase;
import br.com.everton.backendextrato.application.notification.port.in.SendNotificationTestUseCase;
import br.com.everton.backendextrato.application.notification.port.in.TriggerDueBillNotificationsUseCase;
import br.com.everton.backendextrato.application.notification.usecase.command.RegisterMobileSubscriptionCommand;
import br.com.everton.backendextrato.application.notification.usecase.command.RegisterWebSubscriptionCommand;
import br.com.everton.backendextrato.application.notification.usecase.command.SendNotificationTestCommand;
import br.com.everton.backendextrato.application.notification.usecase.result.MobileSubscriptionRegistrationResult;
import br.com.everton.backendextrato.application.notification.usecase.result.WebSubscriptionRegistrationResult;
import br.com.everton.backendextrato.domain.notification.MobileSubscription;
import br.com.everton.backendextrato.domain.notification.NotificationDeliveryResult;
import br.com.everton.backendextrato.domain.notification.NotificationStatus;
import br.com.everton.backendextrato.domain.notification.WebPushSubscription;
import br.com.everton.backendextrato.dto.BillPaymentNotificationRunResponse;
import br.com.everton.backendextrato.dto.MobilePushSubscriptionDeleteRequest;
import br.com.everton.backendextrato.dto.MobilePushSubscriptionDetailsResponse;
import br.com.everton.backendextrato.dto.MobilePushSubscriptionRequest;
import br.com.everton.backendextrato.dto.MobilePushSubscriptionResponse;
import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.dto.PushNotificationStatusResponse;
import br.com.everton.backendextrato.dto.PushNotificationTestRequest;
import br.com.everton.backendextrato.dto.PushNotificationTestResponse;
import br.com.everton.backendextrato.dto.PushSubscriptionDetailsResponse;
import br.com.everton.backendextrato.dto.PushSubscriptionDeleteRequest;
import br.com.everton.backendextrato.dto.PushSubscriptionRequest;
import br.com.everton.backendextrato.dto.PushSubscriptionResponse;
import br.com.everton.backendextrato.service.MongoAuditEventService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificationController {

    private final RegisterWebSubscriptionUseCase registerWebSubscriptionUseCase;
    private final RemoveWebSubscriptionUseCase removeWebSubscriptionUseCase;
    private final ListWebSubscriptionsUseCase listWebSubscriptionsUseCase;
    private final RegisterMobileSubscriptionUseCase registerMobileSubscriptionUseCase;
    private final RemoveMobileSubscriptionUseCase removeMobileSubscriptionUseCase;
    private final ListMobileSubscriptionsUseCase listMobileSubscriptionsUseCase;
    private final GetNotificationStatusUseCase getNotificationStatusUseCase;
    private final SendNotificationTestUseCase sendNotificationTestUseCase;
    private final TriggerDueBillNotificationsUseCase triggerDueBillNotificationsUseCase;
    private final AuthenticatedUserResolver authenticatedUserResolver;
    private final MongoAuditEventService auditEventService;

    public NotificationController(
            RegisterWebSubscriptionUseCase registerWebSubscriptionUseCase,
            RemoveWebSubscriptionUseCase removeWebSubscriptionUseCase,
            ListWebSubscriptionsUseCase listWebSubscriptionsUseCase,
            RegisterMobileSubscriptionUseCase registerMobileSubscriptionUseCase,
            RemoveMobileSubscriptionUseCase removeMobileSubscriptionUseCase,
            ListMobileSubscriptionsUseCase listMobileSubscriptionsUseCase,
            GetNotificationStatusUseCase getNotificationStatusUseCase,
            SendNotificationTestUseCase sendNotificationTestUseCase,
            TriggerDueBillNotificationsUseCase triggerDueBillNotificationsUseCase,
            AuthenticatedUserResolver authenticatedUserResolver,
            MongoAuditEventService auditEventService
    ) {
        this.registerWebSubscriptionUseCase = registerWebSubscriptionUseCase;
        this.removeWebSubscriptionUseCase = removeWebSubscriptionUseCase;
        this.listWebSubscriptionsUseCase = listWebSubscriptionsUseCase;
        this.registerMobileSubscriptionUseCase = registerMobileSubscriptionUseCase;
        this.removeMobileSubscriptionUseCase = removeMobileSubscriptionUseCase;
        this.listMobileSubscriptionsUseCase = listMobileSubscriptionsUseCase;
        this.getNotificationStatusUseCase = getNotificationStatusUseCase;
        this.sendNotificationTestUseCase = sendNotificationTestUseCase;
        this.triggerDueBillNotificationsUseCase = triggerDueBillNotificationsUseCase;
        this.authenticatedUserResolver = authenticatedUserResolver;
        this.auditEventService = auditEventService;
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<PushSubscriptionResponse> register(
            @RequestBody PushSubscriptionRequest request,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            WebSubscriptionRegistrationResult result = registerWebSubscriptionUseCase.execute(
                    new RegisterWebSubscriptionCommand(user.email(), user.name(), request.endpoint(), request.p256dh(), request.auth())
            );
            HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
            WebPushSubscription subscription = result.subscription();
            return ResponseEntity.status(status).body(new PushSubscriptionResponse(
                    subscription.id(),
                    subscription.endpoint(),
                    subscription.userEmail(),
                    subscription.userName(),
                    result.created()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/subscriptions")
    public ResponseEntity<Void> remove(@RequestBody PushSubscriptionDeleteRequest request) {
        try {
            removeWebSubscriptionUseCase.execute(request.endpoint());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<List<PushSubscriptionDetailsResponse>> list(
            HttpServletRequest httpServletRequest
    ) {
        AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
        return ResponseEntity.ok(listWebSubscriptionsUseCase.execute(user.email()).stream()
                .map(subscription -> new PushSubscriptionDetailsResponse(
                        subscription.id(),
                        subscription.endpoint(),
                        subscription.userEmail(),
                        subscription.userName(),
                        subscription.createdAt(),
                        subscription.updatedAt()
                ))
                .toList());
    }

    @PostMapping("/mobile/subscriptions")
    public ResponseEntity<MobilePushSubscriptionResponse> registerMobile(
            @RequestBody MobilePushSubscriptionRequest request,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            MobileSubscriptionRegistrationResult result = registerMobileSubscriptionUseCase.execute(
                    new RegisterMobileSubscriptionCommand(
                            user.email(),
                            user.name(),
                            request.expoPushToken(),
                            request.platform(),
                            request.deviceName(),
                            request.appVersion()
                    )
            );
            HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
            MobileSubscription subscription = result.subscription();
            return ResponseEntity.status(status).body(new MobilePushSubscriptionResponse(
                    subscription.id(),
                    subscription.expoPushToken(),
                    subscription.userEmail(),
                    subscription.userName(),
                    subscription.platform(),
                    subscription.deviceName(),
                    subscription.appVersion(),
                    result.created(),
                    subscription.updatedAt()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/mobile/subscriptions")
    public ResponseEntity<Void> removeMobile(
            @RequestBody MobilePushSubscriptionDeleteRequest request,
            HttpServletRequest httpServletRequest
    ) {
        try {
            authenticatedUserResolver.require(httpServletRequest);
            removeMobileSubscriptionUseCase.execute(request.expoPushToken());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/mobile/subscriptions")
    public ResponseEntity<List<MobilePushSubscriptionDetailsResponse>> listMobile(
            HttpServletRequest httpServletRequest
    ) {
        AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
        return ResponseEntity.ok(listMobileSubscriptionsUseCase.execute(user.email()).stream()
                .map(subscription -> new MobilePushSubscriptionDetailsResponse(
                        subscription.id(),
                        subscription.expoPushToken(),
                        subscription.userEmail(),
                        subscription.userName(),
                        subscription.platform(),
                        subscription.deviceName(),
                        subscription.appVersion(),
                        subscription.createdAt(),
                        subscription.updatedAt()
                ))
                .toList());
    }

    @GetMapping("/status")
    public ResponseEntity<PushNotificationStatusResponse> status() {
        NotificationStatus status = getNotificationStatusUseCase.execute();
        return ResponseEntity.ok(new PushNotificationStatusResponse(status.configured(), status.valid(), status.publicKey()));
    }

    @PostMapping("/teste")
    public ResponseEntity<?> sendTest(@RequestBody PushNotificationTestRequest request, HttpServletRequest httpServletRequest) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String targetUserEmail = hasText(request.userEmail()) ? request.userEmail().trim() : user.email();
            NotificationDeliveryResult result = sendNotificationTestUseCase.execute(
                    new SendNotificationTestCommand(targetUserEmail, request.title(), request.body(), request.url())
            );
            PushNotificationTestResponse response = new PushNotificationTestResponse(
                    result.targetCount(),
                    result.deliveredCount(),
                    result.removedCount(),
                    result.failedCount()
            );
            Map<String, Object> auditPayload = new HashMap<>();
            auditPayload.put("title", request.title());
            auditPayload.put("targetCount", response.targetCount());
            auditPayload.put("delivered", response.deliveredCount());
            auditPayload.put("removed", response.removedCount());
            auditPayload.put("failed", response.failedCount());
            auditEventService.record(
                    targetUserEmail,
                    "notification-test",
                    "notifications",
                    "api",
                    auditPayload
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new NotificationErrorResponse(ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    @PostMapping("/contas-vencendo/disparar")
    public ResponseEntity<?> triggerDueBillNotifications(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            LocalDate effectiveReferenceDate = referenceDate != null ? referenceDate : LocalDate.now();
            BillPaymentNotificationRunResponse response = triggerDueBillNotificationsUseCase.execute(effectiveReferenceDate);
            auditEventService.record(
                    user.email(),
                    "due-bill-notifications",
                    "notifications",
                    "scheduler",
                    Map.of(
                            "referenceDate", response.referenceDate(),
                            "dueUserCount", response.dueUserCount(),
                            "dueBillCount", response.dueBillCount(),
                            "triggeredUserCount", response.triggeredUserCount(),
                            "skippedAlreadySentCount", response.skippedAlreadySentCount(),
                            "usersWithoutSubscriptionsCount", response.usersWithoutSubscriptionsCount(),
                            "deliveredSubscriptionCount", response.deliveredSubscriptionCount(),
                            "failedSubscriptionCount", response.failedSubscriptionCount()
                    )
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new NotificationErrorResponse(ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new NotificationErrorResponse(ex.getMessage()));
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
