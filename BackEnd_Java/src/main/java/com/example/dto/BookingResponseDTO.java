package com.example.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class BookingResponseDTO {

    private Integer bookingId;
    private Integer customerId;
    private LocalDate bookingDate;
    private Integer noOfPax;
    private BigDecimal totalAmount;
    private String status;
    private String statusName;
    private List<TourGuideDTO> guides;
    private String tourName;
    private String tourImage;
    private LocalDate departDate;
    private List<PassengerDTO> passengers;
    private List<PaymentInfoDTO> payments;

    public LocalDate getDepartDate() {
        return departDate;
    }

    public void setDepartDate(LocalDate departDate) {
        this.departDate = departDate;
    }

    // getters & setters
    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Integer getNoOfPax() {
        return noOfPax;
    }

    public void setNoOfPax(Integer noOfPax) {
        this.noOfPax = noOfPax;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public List<TourGuideDTO> getGuides() {
        return guides;
    }

    public void setGuides(List<TourGuideDTO> guides) {
        this.guides = guides;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getTourName() {
        return tourName;
    }

    public void setTourName(String tourName) {
        this.tourName = tourName;
    }

    public String getTourImage() {
        return tourImage;
    }

    public void setTourImage(String tourImage) {
        this.tourImage = tourImage;
    }

    public List<PassengerDTO> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<PassengerDTO> passengers) {
        this.passengers = passengers;
    }

    public List<PaymentInfoDTO> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentInfoDTO> payments) {
        this.payments = payments;
    }
}
