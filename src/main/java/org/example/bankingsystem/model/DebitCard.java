package org.example.bankingsystem.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class DebitCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountNumber;
    private String cardNumber; // 16 digit unique
    private String cardHolderName;
    private LocalDate expiryDate; // 5 years from issue
    private String cvv; // 3 digit unique
    private LocalDateTime issuedDate = LocalDateTime.now();
    private String status = "ACTIVE"; // ACTIVE, BLOCKED, EXPIRED
    private double dailyLimit = 50000; // Default daily limit
    private double currentDaySpent = 0;
    private LocalDate lastResetDate = LocalDate.now();
    private String cardType = "VISA"; // VISA, MASTERCARD
    private String pin;
    private boolean onlineTransactionsEnabled = true;

    public DebitCard() {
    }

    public DebitCard(String accountNumber, String cardHolderName) {
        this.accountNumber = accountNumber;
        this.cardHolderName = cardHolderName;
        this.cardNumber = generateCardNumber();
        this.cvv = generateCVV();
        this.expiryDate = LocalDate.now().plusYears(5);
        this.status = "PENDING"; // Default status pending approval
        this.pin = "1234"; // Default initial PIN
    }

    private String generateCardNumber() {
        StringBuilder sb = new StringBuilder("5");
        for (int i = 0; i < 15; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    private String generateCVV() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            sb.append((int) (Math.random() * 10));
        }
        return sb.toString();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public LocalDateTime getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDateTime issuedDate) {
        this.issuedDate = issuedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getDailyLimit() {
        return dailyLimit;
    }

    public void setDailyLimit(double dailyLimit) {
        this.dailyLimit = dailyLimit;
    }

    public double getCurrentDaySpent() {
        return currentDaySpent;
    }

    public void setCurrentDaySpent(double currentDaySpent) {
        this.currentDaySpent = currentDaySpent;
    }

    public LocalDate getLastResetDate() {
        return lastResetDate;
    }

    public void setLastResetDate(LocalDate lastResetDate) {
        this.lastResetDate = lastResetDate;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public boolean isOnlineTransactionsEnabled() {
        return onlineTransactionsEnabled;
    }

    public void setOnlineTransactionsEnabled(boolean onlineTransactionsEnabled) {
        this.onlineTransactionsEnabled = onlineTransactionsEnabled;
    }
}
