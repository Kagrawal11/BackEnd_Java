package com.example.services;

import java.util.List;

import com.example.dto.BookingCreateRequestDTO;
import com.example.dto.BookingResponseDTO;

public interface BookingService {

    BookingResponseDTO saveBooking(BookingCreateRequestDTO dto);

    BookingResponseDTO getBookingById(Integer bookingId);

    List<BookingResponseDTO> getBookingsByCustomerId(Integer customerId);

    Integer getPaymentStatus(Integer bookingId);

    List<BookingResponseDTO> getAllBookings();

    BookingResponseDTO cancelBooking(Integer bookingId);

}