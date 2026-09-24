package com.example.services.impl;

import com.example.entities.BookingHeader;
import com.example.entities.PaymentMaster;
import com.example.repositories.PaymentRepository;
import com.example.services.EmailService;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

// Sends via Resend's HTTPS API instead of SMTP. Render's free tier blocks
// outbound SMTP (port 587) to smtp.gmail.com - HTTPS (443) works fine, same
// as the Razorpay and Google OAuth calls this app already makes successfully.
@Service
public class EmailServiceImpl implements EmailService {

	private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
	private static final URI RESEND_API_URL = URI.create("https://api.resend.com/emails");

	private final PaymentRepository paymentRepository;
	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(10))
			.build();

	@Value("${resend.api.key}")
	private String resendApiKey;

	@Value("${resend.from.email}")
	private String fromEmail;

	public EmailServiceImpl(PaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	// ---------- SIMPLE EMAIL ----------
	@Override
	public void sendSimpleEmail(String toEmail, String subject, String body) {
		sendViaResend(toEmail, subject, body, null, null);
	}

	// ---------- BOOKING CONFIRMATION ----------
	// Fire-and-forget: this must never block the payment confirmation request.
	@Async
	@Transactional(readOnly = true)
	@Override
	public void sendBookingConfirmation(Long paymentId) {

		PaymentMaster payment = paymentRepository.findById(paymentId.intValue())
				.orElseThrow(() -> new RuntimeException("Payment not found"));

		BookingHeader booking = payment.getBooking();

		String email = booking.getCustomer().getEmail();
		String name = booking.getCustomer().getFirstName();

		if (email == null || email.isBlank()) {
			throw new RuntimeException("Customer email not found");
		}

		String subject = "Booking Confirmed – Booking #" + booking.getId();

		String body = """
                Hello %s,

                Your booking has been successfully confirmed.

                Booking ID: %d

                Thank you for choosing VirtuGo!

                Regards,
                VirtuGo Team
                """.formatted(name, booking.getId());

		sendSimpleEmail(email, subject, body);
	}

	// ---------- INVOICE EMAIL WITH PDF ----------
	@Override
	public void sendInvoiceWithAttachment(Long paymentId, byte[] pdfBytes) {

		PaymentMaster payment = paymentRepository.findById(paymentId.intValue())
				.orElseThrow(() -> new RuntimeException("Payment not found"));

		BookingHeader booking = payment.getBooking();

		String email = booking.getCustomer().getEmail();
		String name = booking.getCustomer().getFirstName();

		if (email == null || email.isBlank()) {
			throw new RuntimeException("Customer email not found");
		}

		String body = """
                Hello %s,

                Please find your invoice attached.

                Booking ID: %d

                Thank you for choosing VirtuGo!

                Regards,
                VirtuGo Team
                """.formatted(name, booking.getId());

		sendViaResend(email, "Invoice – Booking #" + booking.getId(), body,
				"Invoice_" + booking.getId() + ".pdf", pdfBytes);
	}

	private void sendViaResend(String toEmail, String subject, String body, String attachmentName,
			byte[] attachmentBytes) {

		JSONObject payload = new JSONObject();
		payload.put("from", fromEmail);
		payload.put("to", new JSONArray().put(toEmail));
		payload.put("subject", subject);
		payload.put("text", body);

		if (attachmentBytes != null) {
			JSONObject attachment = new JSONObject();
			attachment.put("filename", attachmentName);
			attachment.put("content", Base64.getEncoder().encodeToString(attachmentBytes));
			payload.put("attachments", new JSONArray().put(attachment));
		}

		HttpRequest request = HttpRequest.newBuilder()
				.uri(RESEND_API_URL)
				.timeout(Duration.ofSeconds(15))
				.header("Authorization", "Bearer " + resendApiKey)
				.header("Content-Type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
				.build();

		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() >= 400) {
				throw new RuntimeException("Resend API error " + response.statusCode() + ": " + response.body());
			}
		} catch (Exception e) {
			logger.error("Failed to send email via Resend to {}: {}", toEmail, e.getMessage());
			throw new RuntimeException("Failed to send email", e);
		}
	}
}
