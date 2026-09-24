package com.example.dto;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentInfoDTO {
    private Integer id;
    private String status;
    private BigDecimal amount;
    private String mode;
    private Instant paymentDate;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public Instant getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Instant paymentDate) { this.paymentDate = paymentDate; }
}
