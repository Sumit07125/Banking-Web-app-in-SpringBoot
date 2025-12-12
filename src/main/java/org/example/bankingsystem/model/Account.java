package org.example.bankingsystem.model;

import jakarta.persistence.*;
import java.util.Random;

@Entity
public class Account {

    @Id
    private String accountNumber;
    private String name;
    private String email;
    private String mobileNumber;
    private String mailingAddress;
    private String nomineeName;
    private String pin;
    private double balance;
    private boolean frozen = false; // account is frozen (cannot login/transact)
    private boolean active = true; // account is active
    private int creditScore = 700; // Default credit score
    private double dailyExpenseLimit = -1; // -1 means no limit
    private String nomineeRelation;
    private java.time.LocalDateTime createdDate = java.time.LocalDateTime.now();

    public Account() {
        this.accountNumber = generateAccountNumber();
    }

    private String generateAccountNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++)
            sb.append(random.nextInt(10));
        return sb.toString();
    }

    // Getters and setters
    public String getAccountNumber() {
        return accountNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(String mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

    public String getNomineeName() {
        return nomineeName;
    }

    public void setNomineeName(String nomineeName) {
        this.nomineeName = nomineeName;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getCreditScore() {
        return creditScore;
    }

    public void setCreditScore(int creditScore) {
        this.creditScore = creditScore;
    }

    public double getDailyExpenseLimit() {
        return dailyExpenseLimit;
    }

    public void setDailyExpenseLimit(double dailyExpenseLimit) {
        this.dailyExpenseLimit = dailyExpenseLimit;
    }

    public String getNomineeRelation() {
        return nomineeRelation;
    }

    public void setNomineeRelation(String nomineeRelation) {
        this.nomineeRelation = nomineeRelation;
    }

    public java.time.LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(java.time.LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
}
