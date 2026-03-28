package br.com.everton.backendextrato.controller;

import br.com.everton.backendextrato.dto.NotificationErrorResponse;
import br.com.everton.backendextrato.dto.PushNotificationStatusResponse;
import br.com.everton.backendextrato.dto.PushNotificationTestRequest;
import br.com.everton.backendextrato.dto.PushNotificationTestResponse;
import br.com.everton.backendextrato.dto.PushSubscriptionDetailsResponse;
import br.com.everton.backendextrato.dto.PushSubscriptionDeleteRequest;
import br.com.everton.backendextrato.dto.PushSubscriptionRequest;
import br.com.everton.backendextrato.dto.PushSubscriptionResponse;
import br.com.everton.backendextrato.service.PushNotificationService;
import br.com.everton.backendextrato.service.PushSubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
public class NotificationController {

    private final PushSubscriptionService pushSubscriptionService;
    private final PushNotificationService pushNotificationService;

    public NotificationController(
            PushSubscriptionService pushSubscriptionService,
            PushNotificationService pushNotificationService
    ) {
        this.pushSubscriptionService = pushSubscriptionService;
        this.pushNotificationService = pushNotificationService;
    }

    @PostMapping("/subscriptions")
    public ResponseEntity<PushSubscriptionResponse> register(@RequestBody PushSubscriptionRequest request) {
        try {
            PushSubscriptionResponse response = pushSubscriptionService.register(request);
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
            @RequestParam(required = false) String userEmail
    ) {
        return ResponseEntity.ok(pushSubscriptionService.list(userEmail));
    }

    @GetMapping("/status")
    public ResponseEntity<PushNotificationStatusResponse> status() {
        return ResponseEntity.ok(pushNotificationService.getStatus());
    }

    @PostMapping("/teste")
    public ResponseEntity<?> sendTest(@RequestBody PushNotificationTestRequest request) {
        try {
            PushNotificationTestResponse response = pushNotificationService.sendTest(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new NotificationErrorResponse(ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new NotificationErrorResponse(ex.getMessage()));
        }
    }
}
