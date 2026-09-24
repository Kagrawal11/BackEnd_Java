package com.example.services.impl;

import com.example.dto.CreateOrderRequestDTO;
import com.example.dto.CreateOrderResponseDTO;
import com.example.entities.BookingHeader;
import com.example.entities.PaymentMaster;
import com.example.repositories.BookingRepository;
import com.example.repositories.PaymentRepository;
import com.example.services.EmailService;
import com.example.services.PaymentGatewayService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Service
public class RazorpayServiceImpl implements PaymentGatewayService {

    private final RazorpayClient razorpayClient;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final EmailService emailService;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;

    public RazorpayServiceImpl(RazorpayClient razorpayClient,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            EmailService emailService) {
        this.razorpayClient = razorpayClient;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void confirmPayment(String orderId, String paymentId, Long amount) {

        PaymentMaster payment = paymentRepository
                .findByTransactionRef(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        if ("SUCCESS".equals(payment.getPaymentStatus())) {
            return;
        }

        long expectedAmount = payment.getPaymentAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        if (expectedAmount != amount) {
            throw new RuntimeException("Amount mismatch");
        }

        payment.setTransactionRef(paymentId);
        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentDate(Instant.now());
        paymentRepository.save(payment);

        System.out.println(
                "✅ confirmPayment: Updating Booking ID " + payment.getBooking().getId() + " to status CONFIRMED (2)");

        // Status Update
        bookingRepository.updateBookingStatus(
                payment.getBooking().getId(),
                2);

        try {
            emailService.sendBookingConfirmation(payment.getId().longValue());
        } catch (Exception e) {
            System.err.println("⚠️ Booking confirmation email failed: " + e.getMessage());
        }
    }

    @Override
    public CreateOrderResponseDTO createOrder(CreateOrderRequestDTO requestDTO) {

        BookingHeader booking = bookingRepository.findById(requestDTO.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // SUCCESS guard
        if (paymentRepository.existsByBooking_IdAndPaymentStatus(
                booking.getId(), "SUCCESS")) {
            throw new RuntimeException("Payment already completed");
        }

        // INITIATED reuse
        Optional<PaymentMaster> initiated = paymentRepository.findByBooking_IdAndPaymentStatus(
                booking.getId(), "INITIATED");

        if (initiated.isPresent()) {
            PaymentMaster p = initiated.get();

            long amount = p.getPaymentAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            return new CreateOrderResponseDTO(
                    p.getTransactionRef(), // ORDER ID
                    amount,
                    "INR");
        }

        long amountInPaise = booking.getTotalAmount()
                .multiply(BigDecimal.valueOf(100))
                .longValueExact();

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "BOOKING_" + booking.getId());

        try {
            Order order = razorpayClient.orders.create(orderRequest);

            PaymentMaster payment = new PaymentMaster();
            payment.setBooking(booking);
            payment.setPaymentAmount(booking.getTotalAmount());
            payment.setPaymentMode("RAZORPAY");
            payment.setPaymentStatus("INITIATED");
            payment.setTransactionRef(order.get("id")); // ORDER ID
            payment.setPaymentDate(Instant.now());

            paymentRepository.save(payment);

            return new CreateOrderResponseDTO(
                    order.get("id"),
                    amountInPaise,
                    "INR");

        } catch (Exception e) {
            throw new RuntimeException("Razorpay order creation failed", e);
        }
    }

    @Override
    @Transactional
    public void handleWebhook(String payload, String signature) {

        try {
            if (!Utils.verifyWebhookSignature(payload, signature, webhookSecret)) {
                throw new SecurityException("Invalid webhook signature");
            }
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("Invalid webhook signature", e);
        }

        try {
            System.out.println("🔥 WEBHOOK HIT");
            System.out.println(payload);

            JSONObject json = new JSONObject(payload);
            if (!"payment.captured".equals(json.optString("event")))
                return;

            JSONObject entity = json.getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String razorpayOrderId = entity.getString("order_id");
            String razorpayPaymentId = entity.getString("id");
            long amount = entity.getLong("amount");

            // Lookup by ORDER ID (INITIATED state)
            PaymentMaster payment = paymentRepository
                    .findByTransactionRef(razorpayOrderId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Idempotency
            if ("SUCCESS".equals(payment.getPaymentStatus()))
                return;

            long expected = payment.getPaymentAmount()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            if (expected != amount) {
                throw new RuntimeException("Amount mismatch");
            }

            // overwrite transactionRef with PAYMENT ID
            payment.setTransactionRef(razorpayPaymentId);
            payment.setPaymentStatus("SUCCESS");
            payment.setPaymentDate(Instant.now());
            paymentRepository.save(payment);

            System.out.println(
                    "✅ Webhook: Updating Booking ID " + payment.getBooking().getId() + " to status CONFIRMED (2)");

            bookingRepository.updateBookingStatus(
                    payment.getBooking().getId(),
                    2);

            try {
                emailService.sendBookingConfirmation(payment.getId().longValue());
            } catch (Exception e) {
                System.err.println("⚠️ Booking confirmation email failed: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("❌ Webhook processing failed: " + e.getMessage());
            throw new RuntimeException("Webhook processing failed", e);
        }
    }
}
