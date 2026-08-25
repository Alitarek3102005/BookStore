package com.example.bookstore.service;

import com.example.bookstore.domain.OrderStatus;
import com.example.bookstore.dto.OrderResponse;
import com.stripe.Stripe;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PaymentService {

    @Value("${stripe.api-key}")
    private String stripeApiKey;

    @Value("${stripe.webhook-secret}")
    private String endpointSecret;

    private final OrderService orderService;

    public PaymentService(OrderService orderService) {
        this.orderService = orderService;
    }

    public String generateStripeCheckoutUrl(UUID orderId) throws Exception {
        OrderResponse order = orderService.getById(orderId);
        if (order == null || order.getTotalAmount() == null) {
            throw new RuntimeException("Order or total amount is missing for ID: " + orderId);
        }

        long amountInCents = (long) (order.getTotalAmount() * 100);

        String url = "https://api.stripe.com/v1/checkout/sessions";

        org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setBearerAuth(stripeApiKey);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

        String body = "mode=payment" +
                "&success_url=" + java.net.URLEncoder.encode("http://localhost:4200/order-history?payment=success&orderId=" + orderId, "UTF-8") +
                "&cancel_url=" + java.net.URLEncoder.encode("http://localhost:4200/order-history?payment=canceled", "UTF-8") +
                "&client_reference_id=" + java.net.URLEncoder.encode(orderId.toString(), "UTF-8") +
                "&line_items[0][quantity]=1" +
                "&line_items[0][price_data][currency]=usd" +
                "&line_items[0][price_data][unit_amount]=" + amountInCents +
                "&line_items[0][price_data][product_data][name]=" + java.net.URLEncoder.encode("Virtual Bookstore Order #" + orderId.toString().substring(0, 8), "UTF-8");

        org.springframework.http.HttpEntity<String> request = new org.springframework.http.HttpEntity<>(body, headers);

        try {
            org.springframework.http.ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getBody() != null && response.getBody().containsKey("url")) {
                return (String) response.getBody().get("url");
            }
            throw new RuntimeException("Stripe response did not contain a checkout URL.");
        } catch (Exception e) {
            System.err.println("STRIPE ERROR DETAILS: " + e.getMessage());
            throw e;
        }
    }

    public void processStripeWebhook(String stripeSignature, String payload) throws Exception {
        Event event = Webhook.constructEvent(payload, stripeSignature, endpointSecret);
        handleEvent(event);
    }

    private void handleEvent(Event event) throws com.stripe.exception.EventDataObjectDeserializationException {
        System.out.println("STRIPE EVENT TYPE: " + event.getType());

        if ("checkout.session.completed".equals(event.getType())) {
            Session session = null;
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                session = (Session) event.getDataObjectDeserializer().getObject().get();
            } else {
                session = (Session) event.getDataObjectDeserializer().deserializeUnsafe();
            }

            String clientReferenceId = (session != null) ? session.getClientReferenceId() : null;
            System.out.println("SESSION client_reference_id: " + clientReferenceId);

            if (session != null && clientReferenceId != null) {
                UUID orderId = UUID.fromString(clientReferenceId);
                orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED);
                System.out.println("ORDER " + orderId + " marked SHIPPED via webhook.");
            } else {
                System.out.println("!! No client_reference_id on this session - order was NOT updated. "
                        + "This is expected if the event came from 'stripe trigger' (synthetic test data), "
                        + "not a real checkout flow through generateStripeCheckoutUrl().");
            }
        }
    }
}