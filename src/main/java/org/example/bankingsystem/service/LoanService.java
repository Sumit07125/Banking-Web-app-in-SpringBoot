package org.example.bankingsystem.service;

import org.example.bankingsystem.model.Loan;
import org.example.bankingsystem.model.Account;
import org.example.bankingsystem.model.Transaction;
import org.example.bankingsystem.repository.LoanRepository;
import org.example.bankingsystem.repository.AccountRepository;
import org.example.bankingsystem.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class LoanService {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EmailService emailService;

    // Loan interest rates based on loan amount
    public double calculateInterestRate(double loanAmount) {
        if (loanAmount <= 50000) {
            return 8.5; // 8.5% per annum
        } else if (loanAmount <= 100000) {
            return 8.0; // 8.0% per annum
        } else if (loanAmount <= 500000) {
            return 7.5; // 7.5% per annum
        } else {
            return 7.0; // 7.0% per annum
        }
    }

    // Calculate EMI (Equated Monthly Installment)
    public double calculateEmi(double principal, double rateOfInterest, int months) {
        double monthlyRate = rateOfInterest / (12 * 100);
        double emi = principal * monthlyRate * Math.pow(1 + monthlyRate, months) /
                (Math.pow(1 + monthlyRate, months) - 1);
        return emi;
    }

    // Apply for loan
    public Loan applyForLoan(String accountNumber, double loanAmount, int durationMonths) {
        Optional<Account> account = accountRepository.findById(accountNumber);

        if (!account.isPresent()) {
            throw new RuntimeException("Account not found");
        }

        double rateOfInterest = calculateInterestRate(loanAmount);
        Loan loan = new Loan(accountNumber, loanAmount, durationMonths, rateOfInterest);
        loan.setStatus("PENDING"); // Explicitly ensure PENDING

        // Save loan
        Loan savedLoan = loanRepository.save(loan);

        // Send email notification (Application Received)
        // Note: You might want to update EmailService to handle "Loan Application
        // Received" specifically
        // For now using generic loan email but with clarification in body if possible,
        // or just standard
        emailService.sendEmail(account.get().getEmail(), "Loan Application Received",
                "Your loan application for ₹" + loanAmount + " has been received and is under review. Request ID: "
                        + savedLoan.getLoanId());

        return savedLoan;
    }

    // Approve Loan
    public Loan approveLoan(Long id) {
        Optional<Loan> optionalLoan = loanRepository.findById(id);
        if (!optionalLoan.isPresent()) {
            throw new RuntimeException("Loan request not found");
        }

        Loan loan = optionalLoan.get();
        if (!"PENDING".equals(loan.getStatus())) {
            throw new RuntimeException("Loan is not in PENDING status");
        }

        Optional<Account> account = accountRepository.findById(loan.getAccountNumber());
        if (!account.isPresent()) {
            throw new RuntimeException("Account for loan not found");
        }
        Account acc = account.get();

        // Disburse loan amount
        acc.setBalance(acc.getBalance() + loan.getLoanAmount());
        accountRepository.save(acc);

        // Create transaction record
        Transaction transaction = new Transaction(acc.getAccountNumber(), "LOAN_DISBURSED", loan.getLoanAmount(),
                acc.getBalance(),
                "Loan Disbursement");
        transactionRepository.save(transaction);

        // Update Loan Status
        loan.setStatus("ACTIVE");
        Loan savedLoan = loanRepository.save(loan);

        // Send email
        emailService.sendLoanEmail(acc.getEmail(), acc.getAccountNumber(), loan.getLoanAmount(),
                loan.getDurationMonths(),
                loan.getMonthlyEmi(), loan.getRateOfInterest(), loan.getLoanId());

        return savedLoan;
    }

    // Reject Loan
    public Loan rejectLoan(Long id) {
        Optional<Loan> optionalLoan = loanRepository.findById(id);
        if (!optionalLoan.isPresent()) {
            throw new RuntimeException("Loan request not found");
        }

        Loan loan = optionalLoan.get();
        if (!"PENDING".equals(loan.getStatus())) {
            throw new RuntimeException("Loan is not in PENDING status");
        }

        loan.setStatus("REJECTED");
        loanRepository.save(loan);

        // Notify user
        Optional<Account> account = accountRepository.findById(loan.getAccountNumber());
        if (account.isPresent()) {
            emailService.sendEmail(account.get().getEmail(), "Loan Application Rejected",
                    "Your loan application for ₹" + loan.getLoanAmount() + " has been rejected by the admin.");
        }

        return loan;
    }

    // Get all pending loans
    public List<Loan> getPendingLoans() {
        return loanRepository.findByStatus("PENDING"); // Ensure this method exists in repository
    }

    // Get all loans (Admin view)
    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    // Pay loan EMI
    public boolean payLoanEmi(String accountNumber, Long loanId) {
        Optional<Loan> loan = loanRepository.findById(loanId);
        Optional<Account> account = accountRepository.findById(accountNumber);

        if (!loan.isPresent() || !account.isPresent()) {
            return false;
        }

        Loan loanObj = loan.get();
        Account acc = account.get();

        // Check if loan is active
        if (!loanObj.getStatus().equals("ACTIVE")) {
            return false;
        }

        // Check if account has sufficient balance
        if (acc.getBalance() < loanObj.getMonthlyEmi()) {
            throw new RuntimeException("Insufficient balance for EMI payment");
        }

        // Deduct EMI from account
        acc.setBalance(acc.getBalance() - loanObj.getMonthlyEmi());
        accountRepository.save(acc);

        // Update loan
        loanObj.setMonthsPaid(loanObj.getMonthsPaid() + 1);
        loanObj.setAmountPaid(loanObj.getAmountPaid() + loanObj.getMonthlyEmi());

        // Check if loan is fully paid
        if (loanObj.getMonthsPaid() >= loanObj.getDurationMonths()) {
            loanObj.setStatus("CLOSED");
        }

        loanRepository.save(loanObj);

        // Create transaction record
        Transaction transaction = new Transaction(accountNumber, "LOAN_EMI", loanObj.getMonthlyEmi(), acc.getBalance(),
                "Loan EMI Payment");
        transactionRepository.save(transaction);

        return true;
    }

    // Get all loans for an account
    public List<Loan> getLoans(String accountNumber) {
        return loanRepository.findByAccountNumber(accountNumber);
    }

    // Get active loans for an account
    public List<Loan> getActiveLoans(String accountNumber) {
        return loanRepository.findByAccountNumberAndStatus(accountNumber, "ACTIVE");
    }

    // Auto-debit EMI on 1st of every month (to be scheduled with @Scheduled)
    public void autoDebitEmi() {
        List<Loan> allLoans = loanRepository.findAll();
        for (Loan loan : allLoans) {
            if (loan.getStatus().equals("ACTIVE") && loan.getMonthsPaid() < loan.getDurationMonths()) {
                try {
                    payLoanEmi(loan.getAccountNumber(), loan.getId());
                } catch (Exception e) {
                    System.out.println("❌ EMI auto-debit failed for account: " + loan.getAccountNumber());
                }
            }
        }
    }
}
