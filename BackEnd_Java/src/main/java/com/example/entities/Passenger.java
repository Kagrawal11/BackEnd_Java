package com.example.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "passenger")
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pax_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingHeader booking;

    @Column(name = "pax_name", nullable = false)
    private String paxName;

    @Column(name = "pax_birthdate", nullable = false)
    private LocalDate paxBirthdate;

    @Column(name = "pax_type", nullable = false)
    private String paxType;

    @Column(name = "pax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paxAmount;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BookingHeader getBooking() {
        return booking;
    }

    public void setBooking(BookingHeader booking) {
        this.booking = booking;
    }

    public String getPaxName() {
        return paxName;
    }

    public void setPaxName(String paxName) {
        this.paxName = paxName;
    }

    public LocalDate getPaxBirthdate() {
        return paxBirthdate;
    }

    public void setPaxBirthdate(LocalDate paxBirthdate) {
        this.paxBirthdate = paxBirthdate;
    }

    public String getPaxType() {
        return paxType;
    }

    public void setPaxType(String paxType) {
        this.paxType = paxType;
    }

    public BigDecimal getPaxAmount() {
        return paxAmount;
    }

    public void setPaxAmount(BigDecimal paxAmount) {
        this.paxAmount = paxAmount;
    }

}