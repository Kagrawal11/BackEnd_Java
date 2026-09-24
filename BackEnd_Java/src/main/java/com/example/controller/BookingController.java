package com.example.controller;

import com.example.services.AuthService;
import com.example.services.InvoicePdfService;
import com.example.services.PaymentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.*;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.dto.BookingCreateRequestDTO;
import com.example.dto.BookingResponseDTO;
import com.example.services.BookingService;

@RestController
@RequestMapping("/api/Booking")
public class BookingController {

    private final BookingService bookingService;
    private final InvoicePdfService invoicePdfService;
    private final PaymentService paymentService;
    private final AuthService authService;

    public BookingController(BookingService bookingService,
            InvoicePdfService invoicePdfService,
            PaymentService paymentService,
            AuthService authService) {
        this.bookingService = bookingService;
        this.invoicePdfService = invoicePdfService;
        this.paymentService = paymentService;
        this.authService = authService;
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    // GET ALL BOOKINGS - ADMIN ONLY
    @GetMapping
    public List<BookingResponseDTO> getAllBookings(Authentication authentication) {
        if (!isAdmin(authentication)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return bookingService.getAllBookings();
    }

    // CREATE BOOKING
    @PostMapping
    public BookingResponseDTO createBooking(
            @RequestBody BookingCreateRequestDTO dto) {

        return bookingService.saveBooking(dto);
    }

    // Get bookings by customer ID - scoped to the JWT-authenticated customer
    // (path param kept for frontend compatibility, but must match the caller)
    @GetMapping("/customer/{customerId}")
    public List<BookingResponseDTO> getBookingsByCustomerId1(@PathVariable Integer customerId,
            Authentication authentication) {

        if (!isAdmin(authentication)) {
            Integer ownId = authService.getCustomerIdByEmail(authentication.getName()).getCustomerId();
            if (!ownId.equals(customerId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        return bookingService.getBookingsByCustomerId(customerId);
    }

    // GET BOOKING SUMMARY - only the owning customer (or admin) may view it
    @GetMapping("/{bookingId}")
    public BookingResponseDTO getBooking(@PathVariable Integer bookingId, Authentication authentication) {

        BookingResponseDTO booking = bookingService.getBookingById(bookingId);

        if (!isAdmin(authentication)) {
            Integer ownId = authService.getCustomerIdByEmail(authentication.getName()).getCustomerId();
            if (!ownId.equals(booking.getCustomerId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }

        return booking;
    }

    // PAYMENT STATUS CHECK
    @GetMapping("/status/{bookingId}")
    public Integer getPaymentStatus(@PathVariable Integer bookingId) {
        return bookingService.getPaymentStatus(bookingId);
    }

    // GET INVOICE PDF
    @GetMapping("/invoice/{bookingId}")
    public ResponseEntity<byte[]> getBookingInvoice(@PathVariable Integer bookingId) {
        Integer paymentId = paymentService.findByBookingId(bookingId);
        byte[] pdf = invoicePdfService.generateInvoice(paymentId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=invoice_" + paymentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}