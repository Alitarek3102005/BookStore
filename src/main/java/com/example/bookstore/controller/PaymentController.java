package com.example.bookstore.controller;

import com.example.bookstore.api.PaymentsApi;
import com.example.bookstore.dto.CheckoutSessionResponse;
import com.example.bookstore.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
public class PaymentController implements PaymentsApi {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public ResponseEntity<CheckoutSessionResponse> createCheckoutSession(UUID orderId) {
        try {
            String sessionUrl = paymentService.generateStripeCheckoutUrl(orderId);
            CheckoutSessionResponse response = new CheckoutSessionResponse();
            response.setUrl(URI.create(sessionUrl));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 1. Keep the generated OpenAPI method so the compiler doesn't fail, but ignore it.
    @Override
    public ResponseEntity<String> handleStripeWebhook(String stripeSignature, String body) {
        return ResponseEntity.ok("Ignored");
    }

    // 2. New direct endpoint using byte[] to stop Spring from corrupting the JSON formatting
    @PostMapping("/api/stripe/webhook-direct")
    public ResponseEntity<String> handleStripeWebhookDirect(
            @RequestHeader("Stripe-Signature") String stripeSignature,
            @RequestBody byte[] rawBody) {
        try {
            // Convert raw bytes to string exactly as received. This guarantees the signature matches!
            String payload = new String(rawBody, StandardCharsets.UTF_8);

            paymentService.processStripeWebhook(stripeSignature, payload);
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            System.err.println("Stripe Webhook Error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Webhook Error");
        }
    }
}