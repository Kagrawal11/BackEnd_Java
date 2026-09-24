package com.example.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "booking_header")
public class BookingHeader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id", nullable = false)
    private Integer id;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerMaster customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tour_id", nullable = false)
    private TourMaster tour;

    @Column(name = "no_of_pax", nullable = false)
    private Integer noOfPax;

    @Column(name = "tour_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal tourAmount;

    @Column(name = "taxes", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxes;

    @Column(
    	    name = "total_amount",
    	    precision = 10,
    	    scale = 2
    	)
    	private BigDecimal totalAmount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @ColumnDefault("1")
    @JoinColumn(name = "status_id", nullable = false)
    private BookingStatusMaster status;

    @OneToMany(mappedBy = "booking")
    private Set<Passenger> passengers = new LinkedHashSet<>();

    @OneToMany(mappedBy = "booking")
    private Set<PaymentMaster> paymentMasters = new LinkedHashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public CustomerMaster getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerMaster customer) {
        this.customer = customer;
    }

    public TourMaster getTour() {
        return tour;
    }

    public void setTour(TourMaster tour) {
        this.tour = tour;
    }

    public Integer getNoOfPax() {
        return noOfPax;
    }

    public void setNoOfPax(Integer noOfPax) {
        this.noOfPax = noOfPax;
    }

    public BigDecimal getTourAmount() {
        return tourAmount;
    }

    public void setTourAmount(BigDecimal tourAmount) {
        this.tourAmount = tourAmount;
    }

    public BigDecimal getTaxes() {
        return taxes;
    }

    public void setTaxes(BigDecimal taxes) {
        this.taxes = taxes;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BookingStatusMaster getStatus() {
        return status;
    }

    public void setStatus(BookingStatusMaster status) {
        this.status = status;
    }

    public Set<Passenger> getPassengers() {
        return passengers;
    }

    public void setPassengers(Set<Passenger> passengers) {
        this.passengers = passengers;
    }

    public Set<PaymentMaster> getPaymentMasters() {
        return paymentMasters;
    }

    public void setPaymentMasters(Set<PaymentMaster> paymentMasters) {
        this.paymentMasters = paymentMasters;
    }

}