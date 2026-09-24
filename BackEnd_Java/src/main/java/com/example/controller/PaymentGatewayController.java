package com.example.controller;

import com.example.dto.CreateOrderRequestDTO;
import com.example.dto.CreateOrderResponseDTO;
import com.example.services.PaymentGatewayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment-gateway")
public class PaymentGatewayController {

    private final PaymentGatewayService paymentGatewayService;
    @PostMapping("/confirm-payment")
    public ResponseEntity<String> confirmPayment(
            @RequestParam String orderId,
            @RequestParam String paymentId,
            @RequestParam Long amount) {

        paymentGatewayService.confirmPayment(orderId, paymentId, amount);
        return ResponseEntity.ok("Payment confirmed");
    }


    public PaymentGatewayController(PaymentGatewayService paymentGatewayService) {
        this.paymentGatewayService = paymentGatewayService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<CreateOrderResponseDTO> createOrder(
            @RequestBody CreateOrderRequestDTO requestDTO) {

        return ResponseEntity.ok(paymentGatewayService.createOrder(requestDTO)
        );
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        try {
            paymentGatewayService.handleWebhook(payload, signature);
        } catch (SecurityException e) {
            // signature verification failed - reject, do not process
            return ResponseEntity.badRequest().body("Invalid signature");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok("OK");
    }
    
    
}
