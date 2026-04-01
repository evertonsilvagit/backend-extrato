package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.auth.AuthenticatedUser;
import br.com.everton.backendextrato.auth.AuthenticatedUserResolver;
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
import br.com.everton.backendextrato.service.MobilePushSubscriptionService;
import br.com.everton.backendextrato.service.NotificationDeliveryService;
import br.com.everton.backendextrato.service.PushNotificationService;
import br.com.everton.backendextrato.service.PushSubscriptionService;
import br.com.everton.backendextrato.service.BillPaymentNotificationScheduler;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificationController {

    private final PushSubscriptionService pushSubscriptionService;
    private final MobilePushSubscriptionService mobilePushSubscriptionService;
    private final PushNotificationService pushNotificationService;
    private final NotificationDeliveryService notificationDeliveryService;
    private final BillPaymentNotificationScheduler billPaymentNotificationScheduler;
    private final AuthenticatedUserResolver authenticatedUserResolver;

    public NotificationController(
            PushSubscriptionService pushSubscriptionService,
            MobilePushSubscriptionService mobilePushSubscriptionService,
            PushNotificationService pushNotificationService,
            NotificationDeliveryService notificationDeliveryService,
            BillPaymentNotificationScheduler billPaymentNotificationScheduler,
            AuthenticatedUserResolver authenticatedUserResolver
    ) {
        this.pushSubscriptionService = pushSubscriptionService;
        this.mobilePushSubscriptionService = mobilePushSubscriptionService;
        this.pushNotificationService = pushNotificationService;
        this.notificationDeliveryService = notificationDeliveryService;
        this.billPaymentNotificationScheduler = billPaymentNotificationScheduler;
        this.authenticatedUserResolver = authenticatedUserResolver;
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<PushSubscriptionResponse> register(
            @RequestBody PushSubscriptionRequest request,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            PushSubscriptionResponse response = pushSubscriptionService.register(user.email(), user.name(), request);
            HttpStatus status = response.created() ? HttpStatus.CREATED : HttpStatus.OK;
            return ResponseEntity.status(status).body(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/subscriptions")
    public ResponseEntity<Void> remove(@RequestBody PushSubscriptionDeleteRequest request) {
        try {
            pushSubscriptionService.removeByEndpoint(request.endpoint());
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
        return ResponseEntity.ok(pushSubscriptionService.list(user.email()));
    }

    @PostMapping("/mobile/subscriptions")
    public ResponseEntity<MobilePushSubscriptionResponse> registerMobile(
            @RequestBody MobilePushSubscriptionRequest request,
            HttpServletRequest httpServletRequest
    ) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            MobilePushSubscriptionResponse response = mobilePushSubscriptionService.register(user.email(), user.name(), request);
            HttpStatus status = response.created() ? HttpStatus.CREATED : HttpStatus.OK;
            return ResponseEntity.status(status).body(response);
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
            mobilePushSubscriptionService.removeByExpoPushToken(request.expoPushToken());
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
        return ResponseEntity.ok(mobilePushSubscriptionService.list(user.email()));
    }

    @GetMapping("/status")
    public ResponseEntity<PushNotificationStatusResponse> status() {
        return ResponseEntity.ok(pushNotificationService.getStatus());
    }

    @PostMapping("/teste")
    public ResponseEntity<?> sendTest(@RequestBody PushNotificationTestRequest request, HttpServletRequest httpServletRequest) {
        try {
            AuthenticatedUser user = authenticatedUserResolver.require(httpServletRequest);
            String targetUserEmail = hasText(request.userEmail()) ? request.userEmail().trim() : user.email();
            PushNotificationTestResponse response = notificationDeliveryService.sendToUser(
                    targetUserEmail,
                    request.title(),
                    request.body(),
                    request.url()
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
            authenticatedUserResolver.require(httpServletRequest);
            LocalDate effectiveReferenceDate = referenceDate != null ? referenceDate : LocalDate.now();
            BillPaymentNotificationRunResponse response = billPaymentNotificationScheduler.sendDueBillNotifications(effectiveReferenceDate);
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
