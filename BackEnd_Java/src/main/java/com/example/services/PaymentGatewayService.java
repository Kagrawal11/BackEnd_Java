package com.example.services;

import com.example.dto.CreateOrderRequestDTO;
import com.example.dto.CreateOrderResponseDTO;

public interface PaymentGatewayService {

    /**
     * Create Razorpay order for a booking
     */
    CreateOrderResponseDTO createOrder(CreateOrderRequestDTO requestDTO);

    /**
     * Handle Razorpay webhook (success / failure)
     * Implement later
     */
    void handleWebhook(String payload, String signature);
    
    void confirmPayment(String orderId, String paymentId, Long amount);

    /**
     * Best-effort refund for a booking's successful payment. Never throws -
     * cancellation must succeed even if the refund call fails.
     */
    void refundPayment(Integer bookingId);

}
