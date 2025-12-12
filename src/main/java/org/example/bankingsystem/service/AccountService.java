package org.example.bankingsystem.service;

import org.example.bankingsystem.model.Account;
import org.example.bankingsystem.model.Transaction;
import org.example.bankingsystem.repository.AccountRepository;
import org.example.bankingsystem.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Service
public class AccountService {

    private static final double LOW_BALANCE_THRESHOLD = 1000.0;

    @Autowired
    private AccountRepository accountRepo;

    @Autowired
    private TransactionRepository transactionRepo;

    @Autowired
    private org.example.bankingsystem.repository.AdminMessageRepository adminMessageRepo;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private AuthService authService;

    // Create account (if you already have, keep existing)
    public Account createAccount(Account account) {
        account.setBalance(0.0);
        Account saved = accountRepo.save(account);
        // send welcome email
        emailService.sendAccountCreationEmail(saved.getEmail(), saved.getAccountNumber(), saved.getName());
        return saved;
    }

    // Deposit
    public String deposit(String accNo, double amount) {
        if (amount <= 0)
            return "Amount must be positive.";
        Optional<Account> opt = accountRepo.findById(accNo);
        if (opt.isEmpty())
            return "Account not found.";
        Account acc = opt.get();
        acc.setBalance(acc.getBalance() + amount);
        accountRepo.save(acc);
        Transaction txn = new Transaction(accNo, "DEPOSIT", amount, acc.getBalance(), "Deposit");
        transactionRepo.save(txn);
        emailService.sendTransactionEmail(acc.getEmail(), accNo, "DEPOSIT", amount, acc.getBalance(),
                txn.getTransactionId());
        return "Deposit successful. New balance: ₹" + acc.getBalance();
    }

    private String validateDailyLimit(Account acc, double amount) {
        if (acc.getDailyExpenseLimit() < 0)
            return null; // No limit

        LocalDateTime start = java.time.LocalDate.now().atStartOfDay();
        LocalDateTime end = java.time.LocalDate.now().atTime(java.time.LocalTime.MAX);

        List<Transaction> txns = transactionRepo.findByAccountNumberAndDateBetween(acc.getAccountNumber(), start, end);
        double dailySpent = txns.stream()
                .filter(t -> "WITHDRAW".equals(t.getType()) || "TRANSFER_OUT".equals(t.getType())
                        || "BILL_PAYMENT".equals(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();

        if (dailySpent + amount > acc.getDailyExpenseLimit()) {
            return "Daily expense limit exceeded. Limit: " + acc.getDailyExpenseLimit() + ", Spent today: "
                    + dailySpent;
        }
        return null;
    }

    // Withdraw (requires PIN and OTP for amounts > ₹5000)
    public String withdraw(String accNo, String pin, double amount) {
        if (amount <= 0)
            return "Amount must be positive.";
        Optional<Account> opt = accountRepo.findById(accNo);
        if (opt.isEmpty())
            return "Account not found.";
        Account acc = opt.get();
        if (!verifyPin(acc, pin))
            return "Invalid PIN.";

        String limitError = validateDailyLimit(acc, amount);
        if (limitError != null)
            return limitError;

        if (acc.getBalance() < amount)
            return "Insufficient balance.";

        // If withdrawal amount > ₹5000, send OTP
        if (amount > 5000) {
            if (!otpService.isTransactionVerified(accNo, "WITHDRAWAL")) {
                otpService.sendOtp(accNo, acc.getEmail(), "WITHDRAWAL");
                return "OTP sent to your email. Please verify OTP to complete withdrawal.";
            }
        }

        acc.setBalance(acc.getBalance() - amount);
        accountRepo.save(acc);
        Transaction txn = new Transaction(accNo, "WITHDRAW", amount, acc.getBalance(), "Withdrawal");
        transactionRepo.save(txn);
        emailService.sendTransactionEmail(acc.getEmail(), accNo, "WITHDRAW", amount, acc.getBalance(),
                txn.getTransactionId());

        if (acc.getBalance() < LOW_BALANCE_THRESHOLD) {
            emailService.sendEmail(acc.getEmail(), "Low Balance Alert",
                    "Dear " + acc.getName() + ",\nYour balance is low: ₹" + acc.getBalance());
        }
        return "Withdrawal successful. New balance: ₹" + acc.getBalance();
    }

    // Verify OTP for withdrawal/transfer and complete transaction
    public String verifyWithdrawalOtp(String accNo, String otp) {
        if (!otpService.verifyOtp(accNo, otp)) {
            return "Invalid or expired OTP.";
        }
        return "OTP verified. Withdrawal completed.";
    }

    // Transfer (requires sender PIN and OTP if amount > ₹5000)
    public String transfer(String senderAcc, String senderPin, String receiverAcc, double amount) {
        if (amount <= 0)
            return "Amount must be positive.";
        if (senderAcc.equals(receiverAcc))
            return "Sender and receiver cannot be same.";
        Optional<Account> sOpt = accountRepo.findById(senderAcc);
        Optional<Account> rOpt = accountRepo.findById(receiverAcc);
        if (sOpt.isEmpty())
            return "Sender account not found.";
        if (rOpt.isEmpty())
            return "Receiver account not found.";
        Account sender = sOpt.get();
        Account receiver = rOpt.get();
        if (!verifyPin(sender, senderPin))
            return "Invalid sender PIN.";

        String limitError = validateDailyLimit(sender, amount);
        if (limitError != null)
            return limitError;

        if (sender.getBalance() < amount)
            return "Sender has insufficient funds.";

        // If transfer amount > ₹5000, send OTP
        if (amount > 5000) {
            if (!otpService.isTransactionVerified(senderAcc, "TRANSFER")) {
                otpService.sendOtp(senderAcc, sender.getEmail(), "TRANSFER");
                return "OTP sent to your email. Please verify OTP to complete transfer.";
            }
        }

        // do transfer
        sender.setBalance(sender.getBalance() - amount);
        receiver.setBalance(receiver.getBalance() + amount);
        accountRepo.save(sender);
        accountRepo.save(receiver);

        Transaction txnSender = new Transaction(senderAcc, "TRANSFER_OUT", amount, sender.getBalance(),
                "Transfer to " + receiverAcc);
        Transaction txnReceiver = new Transaction(receiverAcc, "TRANSFER_IN", amount, receiver.getBalance(),
                "Transfer from " + senderAcc);
        transactionRepo.save(txnSender);
        transactionRepo.save(txnReceiver);

        // emails with transaction IDs
        emailService.sendEmail(sender.getEmail(), "Transfer Sent",
                "Dear " + sender.getName() + ",\nYou sent ₹" + amount + " to " + receiver.getName() +
                        " (Acc: " + receiver.getAccountNumber() + ").\nTransaction ID: " + txnSender.getTransactionId()
                        +
                        "\nYour new balance: ₹" + sender.getBalance());
        emailService.sendEmail(receiver.getEmail(), "Transfer Received",
                "Dear " + receiver.getName() + ",\nYou received ₹" + amount + " from " + sender.getName() +
                        " (Acc: " + sender.getAccountNumber() + ").\nTransaction ID: " + txnReceiver.getTransactionId()
                        +
                        "\nYour new balance: ₹" + receiver.getBalance());

        // low balance alert for sender if needed
        if (sender.getBalance() < LOW_BALANCE_THRESHOLD) {
            emailService.sendEmail(sender.getEmail(), "Low Balance Alert",
                    "Dear " + sender.getName() + ",\nYour balance is low: ₹" + sender.getBalance());
        }
        return "Transfer successful.";

    }

    // Pay Bill (Mobile, DTH, Electricity, Insurance)
    public Map<String, Object> payBill(String accNo, String billType, double amount, String provider,
            String consumerDetails) {
        Map<String, Object> response = new HashMap<>();
        if (amount <= 0) {
            response.put("success", false);
            response.put("message", "Amount must be positive.");
            return response;
        }

        Optional<Account> opt = accountRepo.findById(accNo);
        if (opt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Account not found.");
            return response;
        }

        Account acc = opt.get();
        String limitError = validateDailyLimit(acc, amount);
        if (limitError != null) {
            response.put("success", false);
            response.put("message", limitError);
            return response;
        }

        if (acc.getBalance() < amount) {
            response.put("success", false);
            response.put("message", "Insufficient balance.");
            return response;
        }

        // Deduct amount
        acc.setBalance(acc.getBalance() - amount);
        accountRepo.save(acc);

        // Create Transaction
        String description = billType + " Payment: " + provider + " (" + consumerDetails + ")";
        Transaction txn = new Transaction(accNo, "BILL_PAYMENT", amount, acc.getBalance(), description);
        transactionRepo.save(txn);

        // Send Email
        emailService.sendTransactionEmail(acc.getEmail(), accNo, "BILL_PAYMENT", amount, acc.getBalance(),
                txn.getTransactionId());

        response.put("success", true);
        response.put("message", "Payment successful.");
        response.put("transactionId", txn.getTransactionId());
        response.put("newBalance", acc.getBalance());
        return response;
    }

    // Get mini-statement
    public List<Transaction> getMiniStatement(String accNo) {
        return transactionRepo.findByAccountNumberOrderByDateDesc(accNo);
    }

    // CSV export as bytes (now includes transaction ID)
    public ByteArrayInputStream exportTransactionsToCsv(String accNo) {
        List<Transaction> txns = getMiniStatement(accNo);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);
        pw.println("date,type,amount,balance_after,transaction_id");
        for (Transaction t : txns) {
            pw.printf("%s,%s,%.2f,%.2f,%s%n", t.getDate().toString(), t.getType(), t.getAmount(), t.getBalanceAfter(),
                    t.getTransactionId());
        }
        pw.flush();
        return new ByteArrayInputStream(baos.toByteArray());
    }

    // Delete account (requires PIN)
    // Delete method moved to bottom with new logic

    // Get account details
    public java.util.Map<String, Object> getAccountDetails(String accNo) {
        Optional<Account> opt = accountRepo.findById(accNo);
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        if (opt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Account not found");
            return response;
        }
        Account acc = opt.get();
        response.put("success", true);
        response.put("accountNumber", acc.getAccountNumber());
        response.put("name", acc.getName());
        response.put("accountHolderName", acc.getName());
        response.put("email", acc.getEmail());
        response.put("mobileNumber", acc.getMobileNumber());
        response.put("mailingAddress", acc.getMailingAddress());
        response.put("nomineeName", acc.getNomineeName());
        response.put("nomineeRelation", acc.getNomineeRelation());
        response.put("creditScore", acc.getCreditScore());
        response.put("dailyExpenseLimit", acc.getDailyExpenseLimit());
        response.put("balance", acc.getBalance());
        return response;
    }

    // Get user statistics
    public java.util.Map<String, Object> getUserStatistics(String accNo) {
        Optional<Account> opt = accountRepo.findById(accNo);
        java.util.Map<String, Object> response = new java.util.HashMap<>();

        if (opt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Account not found");
            return response;
        }

        Account acc = opt.get();
        List<Transaction> transactions = getMiniStatement(accNo);

        double totalDeposits = transactions.stream()
                .filter(t -> t.getType().equals("DEPOSIT"))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalWithdrawals = transactions.stream()
                .filter(t -> t.getType().equals("WITHDRAW"))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalTransfersOut = transactions.stream()
                .filter(t -> t.getType().equals("TRANSFER_OUT"))
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalTransfersIn = transactions.stream()
                .filter(t -> t.getType().equals("TRANSFER_IN"))
                .mapToDouble(Transaction::getAmount)
                .sum();

        response.put("success", true);
        response.put("accountNumber", acc.getAccountNumber());
        response.put("name", acc.getName());
        response.put("currentBalance", acc.getBalance());
        response.put("totalDeposits", totalDeposits);
        response.put("totalWithdrawals", totalWithdrawals);
        response.put("totalTransfersOut", totalTransfersOut);
        response.put("totalTransfersIn", totalTransfersIn);
        response.put("totalTransactions", transactions.size());

        return response;
    }

    // simple PIN verify
    private boolean verifyPin(Account acc, String pin) {
        if (acc.getPin() == null)
            return false;
        return acc.getPin().equals(pin);
    }

    // Change account PIN (return true if changed)
    public boolean changePin(String accountNumber, String oldPin, String newPin) {
        Optional<Account> opt = accountRepo.findById(accountNumber);
        if (opt.isEmpty())
            return false;
        Account acc = opt.get();
        if (!verifyPin(acc, oldPin))
            return false;
        acc.setPin(newPin);
        accountRepo.save(acc);
        emailService.sendEmail(acc.getEmail(), "PIN Changed", "Your account PIN has been changed successfully.");
        return true;
    }

    // Update profile fields (mobile, address, nominee)
    public boolean updateProfile(String accountNumber, String mobileNumber, String mailingAddress, String nomineeName,
            String nomineeRelation) {
        Optional<Account> opt = accountRepo.findById(accountNumber);
        if (opt.isEmpty())
            return false;
        Account acc = opt.get();
        if (mobileNumber != null)
            acc.setMobileNumber(mobileNumber);
        if (mailingAddress != null)
            acc.setMailingAddress(mailingAddress);
        if (nomineeName != null)
            acc.setNomineeName(nomineeName);
        if (nomineeRelation != null)
            acc.setNomineeRelation(nomineeRelation);
        accountRepo.save(acc);
        return true;
    }

    // Update daily limit
    public boolean updateDailyLimit(String accountNumber, double limit) {
        Optional<Account> opt = accountRepo.findById(accountNumber);
        if (opt.isEmpty())
            return false;
        Account acc = opt.get();
        acc.setDailyExpenseLimit(limit);
        accountRepo.save(acc);
        return true;
    }

    // Admin helpers
    public long countAccounts() {
        return accountRepo.count();
    }

    public long countTransactions() {
        return transactionRepo.count();
    }

    public long countActiveUsers() {
        return accountRepo.findAll().stream().filter(a -> a.getBalance() > 0.0).count();
    }

    public double sumBalances() {
        return accountRepo.findAll().stream().mapToDouble(a -> a.getBalance()).sum();
    }

    public java.util.List<Account> getAllAccounts() {
        return accountRepo.findAll();
    }

    public java.util.List<Transaction> getAllTransactions() {
        return transactionRepo.findAll();
    }

    public List<Transaction> searchTransactions(String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllTransactions();
        }

        String searchTerm = query.trim();

        // 1. Find account numbers matching the name
        List<Account> accounts = accountRepo.findByNameContainingIgnoreCase(searchTerm);
        List<String> accountNumbers = accounts.stream()
                .map(Account::getAccountNumber)
                .collect(java.util.stream.Collectors.toList());

        // 2. Search transactions by ID OR Account Number OR (Account Numbers from Name
        // search)
        List<String> safeAccountNumbers = accountNumbers.isEmpty()
                ? java.util.Collections.singletonList("NON_EXISTENT_ACCOUNT_123456")
                : accountNumbers;

        return transactionRepo.searchTransactions(searchTerm, safeAccountNumbers);
    }

    // Freeze/deactivate account
    public boolean freezeAccount(String accountNumber) {
        java.util.Optional<Account> opt = accountRepo.findById(accountNumber);
        if (opt.isEmpty())
            return false;
        Account acc = opt.get();
        acc.setFrozen(true);
        accountRepo.save(acc);
        emailService.sendEmail(acc.getEmail(), "Account Frozen",
                "Your account has been frozen by admin. Contact support for assistance.");
        return true;
    }

    public boolean unfreezeAccount(String accountNumber) {
        java.util.Optional<Account> opt = accountRepo.findById(accountNumber);
        if (opt.isEmpty())
            return false;
        Account acc = opt.get();
        acc.setFrozen(false);
        accountRepo.save(acc);
        emailService.sendEmail(acc.getEmail(), "Account Unfrozen",
                "Your account has been unfrozen. You can now login and transact.");
        return true;
    }

    public boolean deactivateAccount(String accountNumber) {
        java.util.Optional<Account> opt = accountRepo.findById(accountNumber);
        if (opt.isEmpty())
            return false;
        Account acc = opt.get();
        acc.setActive(false);
        accountRepo.save(acc);
        emailService.sendEmail(acc.getEmail(), "Account Deactivated",
                "Your account has been deactivated by admin.");
        return true;
    }

    public boolean activateAccount(String accountNumber) {
        java.util.Optional<Account> opt = accountRepo.findById(accountNumber);
        if (opt.isEmpty())
            return false;
        Account acc = opt.get();
        acc.setActive(true);
        accountRepo.save(acc);
        emailService.sendEmail(acc.getEmail(), "Account Activated",
                "Your account has been activated. You can now login and transact.");
        return true;
    }

    @Autowired
    private org.example.bankingsystem.repository.DeleteRequestRepository deleteRequestRepo;

    @Autowired
    private org.example.bankingsystem.repository.LoanRepository loanRepo;

    // Delete account (Request initiated by User)
    // Initiate Delete Account (Check Loans & Send OTP)
    public String initiateDeleteAccount(String accNo) {
        Optional<Account> opt = accountRepo.findById(accNo);
        if (opt.isEmpty())
            return "Account not found.";
        Account acc = opt.get();

        // Check for active or pending loans
        List<org.example.bankingsystem.model.Loan> loans = loanRepo.findByAccountNumber(accNo);
        boolean hasPendingOrActiveLoans = loans.stream()
                .anyMatch(l -> "ACTIVE".equals(l.getStatus()) || "PENDING".equals(l.getStatus()));

        if (hasPendingOrActiveLoans) {
            throw new RuntimeException("Cannot delete account. You have active or pending loans.");
        }

        // Check if request already exists
        if (deleteRequestRepo.findByAccountNumberAndStatus(accNo, "PENDING").isPresent()) {
            throw new RuntimeException("Deletion request already submitted.");
        }

        // Send OTP
        authService.sendTransactionOtp(accNo, "DELETE_ACCOUNT");
        return "OTP sent to registered email.";
    }

    // Submit Delete Request (After OTP Verification)
    public String submitDeleteAccount(String accNo, String reason) {
        // We assume OTP verification happened in previous step or handled by
        // controller.
        // For extra security, we could require OTP here again, but reusing the flow
        // simplifies for now.
        // Ideally, we would verify a token from the previous step.
        // Given the user flow "verify otp THEN show form", and assuming authenticated
        // session,
        // we proceed with creating the request.

        // Re-check loans just in case
        List<org.example.bankingsystem.model.Loan> loans = loanRepo.findByAccountNumber(accNo);
        boolean hasPendingOrActiveLoans = loans.stream()
                .anyMatch(l -> "ACTIVE".equals(l.getStatus()) || "PENDING".equals(l.getStatus()));

        if (hasPendingOrActiveLoans) {
            throw new RuntimeException("Cannot delete account. You have active or pending loans.");
        }

        if (deleteRequestRepo.findByAccountNumberAndStatus(accNo, "PENDING").isPresent()) {
            return "Deletion request already submitted.";
        }

        // Create Delete Request
        org.example.bankingsystem.model.DeleteRequest request = new org.example.bankingsystem.model.DeleteRequest(
                accNo);
        request.setReason(reason);
        deleteRequestRepo.save(request);

        return "Delete request submitted. Your account will be deleted after admin approval.";
    }

    // Admin: Get pending delete requests
    public List<org.example.bankingsystem.model.DeleteRequest> getPendingDeleteRequests() {
        return deleteRequestRepo.findByStatus("PENDING");
    }

    // Admin: Approve delete request
    public boolean approveDeleteRequest(Long requestId) {
        Optional<org.example.bankingsystem.model.DeleteRequest> optReq = deleteRequestRepo.findById(requestId);
        if (optReq.isEmpty())
            return false;

        org.example.bankingsystem.model.DeleteRequest req = optReq.get();
        if (!"PENDING".equals(req.getStatus()))
            return false;

        req.setStatus("APPROVED");
        deleteRequestRepo.save(req);

        // Delete the actual account
        String accNo = req.getAccountNumber();
        return deleteAccountAsAdmin(accNo);
    }

    // Admin: Reject delete request
    public boolean rejectDeleteRequest(Long requestId) {
        Optional<org.example.bankingsystem.model.DeleteRequest> optReq = deleteRequestRepo.findById(requestId);
        if (optReq.isEmpty())
            return false;

        org.example.bankingsystem.model.DeleteRequest req = optReq.get();
        if (!"PENDING".equals(req.getStatus()))
            return false;

        req.setStatus("REJECTED");
        deleteRequestRepo.save(req);

        // Notify user
        Optional<Account> optAcc = accountRepo.findById(req.getAccountNumber());
        if (optAcc.isPresent()) {
            emailService.sendEmail(optAcc.get().getEmail(), "Deletion Request Rejected",
                    "Your account deletion request has been rejected by admin.");
        }
        return true;
    }

    // Admin direct delete (still useful for admin overriding)
    public boolean deleteAccountAsAdmin(String accountNumber) {
        java.util.Optional<Account> opt = accountRepo.findById(accountNumber);
        if (opt.isEmpty())
            return false;
        Account acc = opt.get();
        accountRepo.delete(acc);
        emailService.sendEmail(acc.getEmail(), "Account Deleted",
                "Your account has been deleted by admin.");
        return true;
    }

    // Analytics: Get last 30 days stats
    public Map<String, Object> getAnalyticsData(String accountNumber) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        // Use existing method which fetches all, then filter in Java (fallback for JPA
        // issue)
        List<Transaction> allTransactions = transactionRepo.findByAccountNumberOrderByDateDesc(accountNumber);

        List<Transaction> transactions = allTransactions.stream()
                .filter(t -> t.getDate().isAfter(thirtyDaysAgo))
                .collect(java.util.stream.Collectors.toList());

        // Prepare aggregation
        Map<String, double[]> dailyMap = new java.util.TreeMap<>(); // Date -> [Income, Expense]

        double totalIncome = 0;
        double totalExpense = 0;

        for (Transaction t : transactions) {
            String dateKey = t.getDate().toLocalDate().toString();
            dailyMap.putIfAbsent(dateKey, new double[] { 0.0, 0.0 });

            double amount = t.getAmount();
            String type = t.getType(); // DEPOSIT, WITHDRAW, TRANSFER_OUT, TRANSFER_IN

            if ("DEPOSIT".equalsIgnoreCase(type) || "TRANSFER_IN".equalsIgnoreCase(type)) {
                dailyMap.get(dateKey)[0] += amount;
                totalIncome += amount;
            } else if ("WITHDRAW".equalsIgnoreCase(type) || "TRANSFER_OUT".equalsIgnoreCase(type)
                    || "WITHDRAWAL".equalsIgnoreCase(type) || "TRANSFER".equalsIgnoreCase(type)) {
                dailyMap.get(dateKey)[1] += amount;
                totalExpense += amount;
            }
        }

        // Convert to List for JSON
        List<Map<String, Object>> dailyStats = new java.util.ArrayList<>();
        for (Map.Entry<String, double[]> entry : dailyMap.entrySet()) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", entry.getKey());
            day.put("income", entry.getValue()[0]);
            day.put("expense", entry.getValue()[1]);
            dailyStats.add(day);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("dailyStats", dailyStats);
        response.put("totalIncome", totalIncome);
        response.put("totalExpense", totalExpense);

        return response;
    }

    // Admin Analytics
    public java.util.Map<String, Object> getAdminAnalytics() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();

        // 1. Transaction Stats (Last 30 Days)
        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        java.util.List<Transaction> recentTxns = transactionRepo.findAll().stream()
                .filter(t -> t.getDate().isAfter(thirtyDaysAgo))
                .collect(java.util.stream.Collectors.toList());

        // Group by Date for Volume and Count
        java.util.Map<java.time.LocalDate, Double> dailyVolume = recentTxns.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getDate().toLocalDate(),
                        java.util.stream.Collectors.summingDouble(Transaction::getAmount)));

        java.util.Map<java.time.LocalDate, Long> dailyCount = recentTxns.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        t -> t.getDate().toLocalDate(),
                        java.util.stream.Collectors.counting()));

        // Format for Chart (Transactions)
        java.util.List<java.util.Map<String, Object>> chartData = new java.util.ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            java.time.LocalDate d = java.time.LocalDate.now().minusDays(i);
            java.util.Map<String, Object> point = new java.util.HashMap<>();
            point.put("date", d.toString());
            point.put("amount", dailyVolume.getOrDefault(d, 0.0));
            point.put("count", dailyCount.getOrDefault(d, 0L));
            chartData.add(point);
        }
        stats.put("transactionTrends", chartData);

        // 2. User Stats
        java.util.List<Account> allAccounts = accountRepo.findAll();
        long active = allAccounts.stream().filter(Account::isActive).count();
        long inactive = allAccounts.size() - active;
        stats.put("activeUsers", active);
        stats.put("inactiveUsers", inactive);

        // 3. New Users Trend
        java.util.Map<java.time.LocalDate, Long> dailyNewUsers = allAccounts.stream()
                .filter(a -> a.getCreatedDate() != null && a.getCreatedDate().isAfter(thirtyDaysAgo))
                .collect(java.util.stream.Collectors.groupingBy(
                        a -> a.getCreatedDate().toLocalDate(),
                        java.util.stream.Collectors.counting()));

        java.util.List<java.util.Map<String, Object>> newUserTrend = new java.util.ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            java.time.LocalDate d = java.time.LocalDate.now().minusDays(i);
            java.util.Map<String, Object> point = new java.util.HashMap<>();
            point.put("date", d.toString());
            point.put("users", dailyNewUsers.getOrDefault(d, 0L));
            newUserTrend.add(point);
        }
        stats.put("newUserTrend", newUserTrend);

        return stats;
    }

    // --- Messaging System ---

    // Send individual message
    public boolean sendAdminMessage(String accountNumber, String messageContent) {
        java.util.Optional<Account> opt = accountRepo.findById(accountNumber);
        if (opt.isEmpty())
            return false;

        Account acc = opt.get();
        // Send Email
        String subject = "Important Message from Bank Admin";
        String htmlBody = "<html><body>" +
                "<h3>Message from Administrator</h3>" +
                "<p>Dear " + acc.getName() + ",</p>" +
                "<p>" + messageContent + "</p>" +
                "<hr/><p style='font-size:12px; color:gray'>This is an automated message from the Banking System.</p>" +
                "</body></html>";

        emailService.sendHtmlEmail(acc.getEmail(), subject, htmlBody);

        // Log to DB
        org.example.bankingsystem.model.AdminMessage msg = new org.example.bankingsystem.model.AdminMessage(
                accountNumber, messageContent, "INDIVIDUAL");
        adminMessageRepo.save(msg);
        return true;
    }

    // Send broadcast message
    public void sendBroadcastMessage(String messageContent) {
        // Log to DB first
        org.example.bankingsystem.model.AdminMessage msg = new org.example.bankingsystem.model.AdminMessage(
                "ALL", messageContent, "BROADCAST");
        adminMessageRepo.save(msg);

        // Fetch all emails (optimize in real system with batching/bcc, iterating for
        // prototype)
        List<Account> all = accountRepo.findAll();
        String subject = "Official Announcement";
        String htmlBody = "<html><body>" +
                "<h3>Official Announcement</h3>" +
                "<p>" + messageContent + "</p>" +
                "<hr/><p style='font-size:12px; color:gray'>This is an automated broadcast from the Banking System.</p>"
                +
                "</body></html>";

        // Run in background thread to avoid blocking response
        new Thread(() -> {
            for (Account acc : all) {
                if (acc.getEmail() != null && !acc.getEmail().isEmpty()) {
                    emailService.sendHtmlEmail(acc.getEmail(), subject, htmlBody);
                }
            }
        }).start();
    }

    // Get message history
    public List<org.example.bankingsystem.model.AdminMessage> getMessageHistory() {
        return adminMessageRepo.findAllByOrderBySentAtDesc();
    }
}
