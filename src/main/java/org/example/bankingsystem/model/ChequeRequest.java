package org.example.bankingsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class ChequeRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountNumber;
    private String type; // NEW_BOOK, STOP_PAYMENT
    private String chequeNumber; // optional for STOP_PAYMENT or status checks
    private String status = "PENDING"; // PENDING, PROCESSING, COMPLETED
    private LocalDateTime createdAt = LocalDateTime.now();

    public ChequeRequest() {
    }

    public ChequeRequest(String accountNumber, String type, String chequeNumber) {
        this.accountNumber = accountNumber;
        this.type = type;
        this.chequeNumber = chequeNumber;
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChequeNumber() {
        return chequeNumber;
    }

    public void setChequeNumber(String chequeNumber) {
        this.chequeNumber = chequeNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
