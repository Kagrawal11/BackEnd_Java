package com.example.services.impl;

import com.example.dto.BookingCreateRequestDTO;
import com.example.dto.BookingResponseDTO;
import com.example.dto.PassengerDTO;
import com.example.dto.PaymentInfoDTO;
import com.example.dto.TourGuideDTO;
import com.example.entities.*;
import com.example.repositories.BookingRepository;
import com.example.services.BookingService;
import com.example.services.PaymentGatewayService;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class BookingServicesImpl implements BookingService {

    // Matches the seeded booking_status_master rows (PENDING, CONFIRMED, CANCELLED).
    private static final Integer CANCELLED_STATUS_ID = 3;

    private final BookingRepository bookingRepository;
    private final EntityManager entityManager;
    private final PaymentGatewayService paymentGatewayService;

    public BookingServicesImpl(
            BookingRepository bookingRepository,
            EntityManager entityManager,
            PaymentGatewayService paymentGatewayService) {
        this.bookingRepository = bookingRepository;
        this.entityManager = entityManager;
        this.paymentGatewayService = paymentGatewayService;
    }

    // NEW METHOD
    @Override
    public List<BookingResponseDTO> getBookingsByCustomerId(Integer customerId) {
        return bookingRepository.findByCustomerId(customerId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponseDTO saveBooking(BookingCreateRequestDTO dto) {
        try {
            // ✅ ID-only references
            CustomerMaster customerRef = entityManager.getReference(CustomerMaster.class, dto.getCustomerId());
            TourMaster tourRef = entityManager.getReference(TourMaster.class, dto.getTourId());

            // Check if statusId is provided, else default to 1
            Integer statusId = (dto.getStatusId() != null && dto.getStatusId() != 0) ? dto.getStatusId() : 1;

            BookingStatusMaster statusRef;
            try {
                statusRef = entityManager.getReference(BookingStatusMaster.class, statusId);
                // Trigger a small check to see if it exists (prevents lazy exception later if
                // it doesn't)
                statusRef.getId();
            } catch (Exception e) {
                // If ID 1 or provided ID is missing, we might hit an issue.
                // For now, let's assume it should exist or handled via DB defaults.
                statusRef = entityManager.getReference(BookingStatusMaster.class, statusId);
            }

            BigDecimal tourAmount = dto.getTourAmount() != null ? dto.getTourAmount() : BigDecimal.ZERO;
            BigDecimal taxes = dto.getTaxes() != null ? dto.getTaxes() : BigDecimal.ZERO;

            BookingHeader booking = new BookingHeader();
            booking.setBookingDate(LocalDate.now());
            booking.setCustomer(customerRef);
            booking.setTour(tourRef);
            booking.setStatus(statusRef);
            booking.setNoOfPax(dto.getNoOfPax());
            booking.setTourAmount(tourAmount);
            booking.setTaxes(taxes);
            booking.setTotalAmount(tourAmount.add(taxes));

            BookingHeader saved = bookingRepository.save(booking);
            return mapToResponseDTO(saved);
        } catch (Exception e) {
            System.err.println("Error saving booking: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save booking: " + e.getMessage());
        }
    }

    @Override
    public BookingResponseDTO getBookingById(Integer bookingId) {

        BookingHeader booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        return mapToResponseDTO(booking);
    }

    // 🔁 COMMON MAPPER
    private BookingResponseDTO mapToResponseDTO(BookingHeader booking) {

        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setBookingId(booking.getId());
        dto.setBookingDate(booking.getBookingDate());
        dto.setNoOfPax(booking.getNoOfPax());
        dto.setTotalAmount(booking.getTotalAmount());

        if (booking.getCustomer() != null) {
            dto.setCustomerId(booking.getCustomer().getId());
        }

        // ✅ STATUS STRING
        if (booking.getStatus() != null) {
            dto.setStatus(booking.getStatus().getStatusName());
            dto.setStatusName(booking.getStatus().getStatusName());
        }

        // ✅ TOUR GUIDES + NAME/IMAGE (name = category name, same as InvoiceServiceImpl)
        if (booking.getTour() != null) {
            if (booking.getTour().getCategory() != null) {
                dto.setTourName(booking.getTour().getCategory().getCategoryName());
                dto.setTourImage(booking.getTour().getCategory().getImagePath());
            }
            if (booking.getTour().getDeparture() != null) {
                dto.setDepartDate(booking.getTour().getDeparture().getDepartDate());
            }

            if (booking.getTour().getTourGuides() != null) {
                List<TourGuideDTO> guides = booking.getTour().getTourGuides().stream()
                        .map(g -> {
                            TourGuideDTO gDto = new TourGuideDTO();
                            gDto.setId(g.getId());
                            gDto.setName(g.getName());
                            gDto.setEmail(g.getEmail());
                            gDto.setPhone(g.getPhone());
                            return gDto;
                        })
                        .collect(Collectors.toList());
                dto.setGuides(guides);
            }
        }

        // ✅ PASSENGERS
        if (booking.getPassengers() != null) {
            List<PassengerDTO> passengers = booking.getPassengers().stream()
                    .map(p -> {
                        PassengerDTO pDto = new PassengerDTO();
                        pDto.setId(p.getId());
                        pDto.setBookingId(booking.getId());
                        pDto.setPaxName(p.getPaxName());
                        pDto.setPaxBirthdate(p.getPaxBirthdate());
                        pDto.setPaxType(p.getPaxType());
                        pDto.setPaxAmount(p.getPaxAmount());
                        return pDto;
                    })
                    .collect(Collectors.toList());
            dto.setPassengers(passengers);
        }

        // ✅ PAYMENTS
        if (booking.getPaymentMasters() != null) {
            List<PaymentInfoDTO> payments = booking.getPaymentMasters().stream()
                    .map(p -> {
                        PaymentInfoDTO payDto = new PaymentInfoDTO();
                        payDto.setId(p.getId());
                        payDto.setStatus(p.getPaymentStatus());
                        payDto.setAmount(p.getPaymentAmount());
                        payDto.setMode(p.getPaymentMode());
                        payDto.setPaymentDate(p.getPaymentDate());
                        return payDto;
                    })
                    .collect(Collectors.toList());
            dto.setPayments(payments);
        }

        return dto;
    }

    @Override
    public Integer getPaymentStatus(Integer bookingId) {

        Integer statusId = bookingRepository.findStatusIdByBookingId(bookingId);

        if (statusId == null) {
            throw new RuntimeException("Booking not found");
        }

        return statusId;
    }

    @Override
    public List<BookingResponseDTO> getAllBookings() {
        return bookingRepository.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BookingResponseDTO cancelBooking(Integer bookingId) {
        BookingHeader booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (CANCELLED_STATUS_ID.equals(booking.getStatus().getId())) {
            return mapToResponseDTO(booking);
        }

        // Best-effort refund of any successful payment - never blocks cancellation.
        paymentGatewayService.refundPayment(bookingId);

        bookingRepository.updateBookingStatus(bookingId, CANCELLED_STATUS_ID);
        entityManager.refresh(booking);

        return mapToResponseDTO(booking);
    }

}