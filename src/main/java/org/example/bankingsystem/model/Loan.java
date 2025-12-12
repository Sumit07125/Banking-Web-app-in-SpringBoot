package org.example.bankingsystem.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String accountNumber;
    private double loanAmount;
    private int durationMonths;
    private double rateOfInterest;
    private double monthlyEmi;
    private double totalAmount; // Principal + Interest
    private LocalDateTime createdAt = LocalDateTime.now();
    private String status = "PENDING"; // PENDING, ACTIVE, CLOSED, REJECTED
    private int monthsPaid = 0;
    private double amountPaid = 0;
    private String loanId; // Unique transaction ID like TXN123456

    public Loan() {
    }

    public Loan(String accountNumber, double loanAmount, int durationMonths, double rateOfInterest) {
        this.accountNumber = accountNumber;
        this.loanAmount = loanAmount;
        this.durationMonths = durationMonths;
        this.rateOfInterest = rateOfInterest;
        this.totalAmount = calculateTotalAmount(loanAmount, rateOfInterest, durationMonths);
        this.monthlyEmi = this.totalAmount / durationMonths;
        this.loanId = generateLoanId();
    }

    private double calculateTotalAmount(double principal, double rate, int months) {
        double monthlyRate = rate / (12 * 100);
        double totalInterest = principal * monthlyRate * months;
        return principal + totalInterest;
    }

    private String generateLoanId() {
        return "LOAN" + System.currentTimeMillis();
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

    public double getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(double loanAmount) {
        this.loanAmount = loanAmount;
    }

    public int getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(int durationMonths) {
        this.durationMonths = durationMonths;
    }

    public double getRateOfInterest() {
        return rateOfInterest;
    }

    public void setRateOfInterest(double rateOfInterest) {
        this.rateOfInterest = rateOfInterest;
    }

    public double getMonthlyEmi() {
        return monthlyEmi;
    }

    public void setMonthlyEmi(double monthlyEmi) {
        this.monthlyEmi = monthlyEmi;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getMonthsPaid() {
        return monthsPaid;
    }

    public void setMonthsPaid(int monthsPaid) {
        this.monthsPaid = monthsPaid;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }
}
