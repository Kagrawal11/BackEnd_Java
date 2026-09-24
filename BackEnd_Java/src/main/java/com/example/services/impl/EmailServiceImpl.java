package com.example.services.impl;

import com.example.entities.BookingHeader;
import com.example.entities.PaymentMaster;
import com.example.repositories.PaymentRepository;
import com.example.services.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailServiceImpl implements EmailService {

	private final JavaMailSender mailSender;
	private final PaymentRepository paymentRepository;

	@Value("${spring.mail.username}")
	private String FROM_EMAIL;

	public EmailServiceImpl(JavaMailSender mailSender,
							PaymentRepository paymentRepository) {
		this.mailSender = mailSender;
		this.paymentRepository = paymentRepository;
	}

	// ---------- SIMPLE EMAIL ----------
	@Override
	public void sendSimpleEmail(String toEmail, String subject, String body) {

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(FROM_EMAIL);
		message.setTo(toEmail);
		message.setSubject(subject);
		message.setText(body);

		mailSender.send(message);
	}

	// ---------- BOOKING CONFIRMATION ----------
	// Fire-and-forget: this must never block the payment confirmation request.
	// Render's free tier can't complete outbound SMTP to Gmail (port blocked/
	// unreachable), and mailSender.send() has no configured timeout, so a
	// synchronous call here was hanging the whole confirm-payment response for
	// well over a minute even though the payment itself already succeeded.
	@Async
	@Transactional(readOnly = true)
	@Override
	public void sendBookingConfirmation(Long paymentId) {

		PaymentMaster payment = paymentRepository.findById(paymentId.intValue())
				.orElseThrow(() -> new RuntimeException("Payment not found"));

		BookingHeader booking = payment.getBooking();

		String email = booking.getCustomer().getEmail();
		String name  = booking.getCustomer().getFirstName();

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
		String name  = booking.getCustomer().getFirstName();

		if (email == null || email.isBlank()) {
			throw new RuntimeException("Customer email not found");
		}

		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper =
					new MimeMessageHelper(message, true);

			helper.setFrom(FROM_EMAIL);
			helper.setTo(email);
			helper.setSubject("Invoice – Booking #" + booking.getId());

			helper.setText("""
                    Hello %s,

                    Please find your invoice attached.

                    Booking ID: %d

                    Thank you for choosing VirtuGo!

                    Regards,
                    VirtuGo Team
                    """.formatted(name, booking.getId()));

			helper.addAttachment(
					"Invoice_" + booking.getId() + ".pdf",
					new ByteArrayResource(pdfBytes)
			);

			mailSender.send(message);

		} catch (MessagingException e) {
			throw new RuntimeException("Failed to send invoice email", e);
		}
	}
}
