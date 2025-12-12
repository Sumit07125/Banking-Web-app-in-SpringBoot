package org.example.bankingsystem.controller;

import org.example.bankingsystem.model.Account;
import org.example.bankingsystem.model.Transaction;
import org.example.bankingsystem.model.Loan;
import org.example.bankingsystem.model.DebitCard;

import org.example.bankingsystem.service.AccountService;
import org.example.bankingsystem.service.LoanService;
import org.example.bankingsystem.service.DebitCardService;
import org.example.bankingsystem.service.AuthService;
import org.example.bankingsystem.service.LoginHistoryService;
import org.example.bankingsystem.service.ChequeService;
import org.example.bankingsystem.model.ChequeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@CrossOrigin(origins = "http://localhost:3000")
public class RestAccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AuthService authService;

    @Autowired
    private LoanService loanService;

    @Autowired
    private DebitCardService debitCardService;

    @Autowired
    private ChequeService chequeService;

    @Autowired
    private LoginHistoryService loginHistoryService;

    /**
     * Create a new account
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody Account account) {
        try {
            Account saved = accountService.createAccount(account);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Account created successfully");
            response.put("accountNumber", saved.getAccountNumber());
            response.put("account", saved);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Deposit money
     */
    @PostMapping("/deposit")
    public ResponseEntity<Map<String, Object>> deposit(
            @RequestParam String accountNumber,
            @RequestParam double amount) {
        String result = accountService.deposit(accountNumber, amount);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.contains("successful"));
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    /**
     * Withdraw money
     */
    @PostMapping("/withdraw")
    public ResponseEntity<Map<String, Object>> withdraw(
            @RequestParam String accountNumber,
            @RequestParam String pin,
            @RequestParam double amount) {
        String result = accountService.withdraw(accountNumber, pin, amount);
        Map<String, Object> response = new HashMap<>();
        boolean requiresOtp = result.contains("OTP sent");
        response.put("success", !requiresOtp && result.contains("successful"));
        response.put("message", result);
        response.put("requiresOtp", requiresOtp);
        return ResponseEntity.ok(response);
    }

    /**
     * Verify OTP for withdrawal
     */
    @PostMapping("/verify-withdrawal-otp")
    public ResponseEntity<Map<String, Object>> verifyWithdrawalOtp(
            @RequestParam String accountNumber,
            @RequestParam String otp) {
        String result = accountService.verifyWithdrawalOtp(accountNumber, otp);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.contains("verified"));
        response.put("message", result);
        return ResponseEntity.ok(response);
    }

    /**
     * Transfer money between accounts
     */
    @PostMapping("/transfer")
    public ResponseEntity<Map<String, Object>> transfer(
            @RequestParam String senderAcc,
            @RequestParam String senderPin,
            @RequestParam String receiverAcc,
            @RequestParam double amount) {
        String result = accountService.transfer(senderAcc, senderPin, receiverAcc, amount);
        Map<String, Object> response = new HashMap<>();
        boolean requiresOtp = result.contains("OTP sent");
        response.put("success", !requiresOtp && result.contains("successful"));
        response.put("requiresOtp", requiresOtp);
        return ResponseEntity.ok(response);
    }

    /**
     * Pay Bill
     */
    @PostMapping("/pay-bill")
    public ResponseEntity<Map<String, Object>> payBill(
            @RequestParam String accountNumber,
            @RequestParam String billType,
            @RequestParam double amount,
            @RequestParam String provider,
            @RequestParam String consumerDetails) {
        Map<String, Object> result = accountService.payBill(accountNumber, billType, amount, provider, consumerDetails);
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    /**
     * Download CSV statement
     */
    @GetMapping("/statement/{accNo}/download")
    public ResponseEntity<InputStreamResource> downloadCsv(@PathVariable String accNo) {
        ByteArrayInputStream bais = accountService.exportTransactionsToCsv(accNo);
        InputStreamResource resource = new InputStreamResource(bais);
        String filename = "mini_statement_" + accNo + ".csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }

    /**
     * Get statement (transactions) for an account
     */
    @GetMapping("/statement")
    public ResponseEntity<Map<String, Object>> getStatement(@RequestParam String accountNumber) {
        List<Transaction> transactions = accountService.getMiniStatement(accountNumber);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("transactions", transactions);
        return ResponseEntity.ok(response);
    }

    /**
     * Initiate delete account (Check loans, send OTP)
     */
    @PostMapping("/delete/initiate")
    public ResponseEntity<Map<String, Object>> initiateDeleteAccount(@RequestParam String accountNumber) {
        try {
            String result = accountService.initiateDeleteAccount(accountNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Submit delete request (After OTP verification)
     */
    @PostMapping("/delete/submit")
    public ResponseEntity<Map<String, Object>> submitDeleteAccount(
            @RequestParam String accountNumber,
            @RequestParam String reason) {
        try {
            String result = accountService.submitDeleteAccount(accountNumber, reason);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get account details
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<Map<String, Object>> getAccount(@PathVariable String accountNumber) {
        Map<String, Object> response = accountService.getAccountDetails(accountNumber);
        if (response != null) {
            return ResponseEntity.ok(response);
        }
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "Account not found");
        return ResponseEntity.notFound().build();
    }

    /**
     * Get user statistics
     */
    @GetMapping("/statistics/{accountNumber}")
    public ResponseEntity<Map<String, Object>> getStatistics(@PathVariable String accountNumber) {
        Map<String, Object> response = accountService.getUserStatistics(accountNumber);
        if ((Boolean) response.get("success")) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Apply for loan
     */
    /**
     * Apply for loan
     */
    @PostMapping("/loan/apply")
    public ResponseEntity<Map<String, Object>> applyForLoan(
            @RequestParam String accountNumber,
            @RequestParam double loanAmount,
            @RequestParam int durationMonths) {
        try {
            Loan loan = loanService.applyForLoan(accountNumber, loanAmount, durationMonths);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan application submitted for review.");
            response.put("loanId", loan.getLoanId());
            response.put("monthlyEmi", loan.getMonthlyEmi());
            response.put("rateOfInterest", loan.getRateOfInterest());
            response.put("loan", loan);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: Get pending loans
     */
    @GetMapping("/admin/loan/requests")
    public ResponseEntity<Map<String, Object>> getPendingLoans() {
        try {
            List<Loan> loans = loanService.getPendingLoans();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("loans", loans);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: Approve loan
     */
    @PostMapping("/admin/loan/approve")
    public ResponseEntity<Map<String, Object>> approveLoan(@RequestParam Long loanId) {
        try {
            Loan loan = loanService.approveLoan(loanId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan approved successfully.");
            response.put("loan", loan);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: Reject loan
     */
    @PostMapping("/admin/loan/reject")
    public ResponseEntity<Map<String, Object>> rejectLoan(@RequestParam Long loanId) {
        try {
            Loan loan = loanService.rejectLoan(loanId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Loan rejected.");
            response.put("loan", loan);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get interest rate for loan amount
     */
    @GetMapping("/loan/interest-rate")
    public ResponseEntity<Map<String, Object>> getInterestRate(@RequestParam double loanAmount) {
        double rate = loanService.calculateInterestRate(loanAmount);
        Map<String, Object> response = new HashMap<>();
        response.put("loanAmount", loanAmount);
        response.put("rateOfInterest", rate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all loans for account
     */
    @GetMapping("/loans/{accountNumber}")
    public ResponseEntity<Map<String, Object>> getLoans(@PathVariable String accountNumber) {
        List<Loan> loans = loanService.getLoans(accountNumber);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("loans", loans);
        return ResponseEntity.ok(response);
    }

    /**
     * Issue debit card
     */
    @PostMapping("/debit-card/issue")
    public ResponseEntity<Map<String, Object>> issueDebitCard(@RequestParam String accountNumber) {
        try {
            DebitCard card = debitCardService.issueDebitCard(accountNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Debit card request submitted successfully. Awaiting approval.");
            response.put("card", card);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Toggle online transactions
     */
    @PostMapping("/debit-card/toggle-online")
    public ResponseEntity<Map<String, Object>> toggleOnlineTransactions(@RequestParam String cardNumber) {
        boolean ok = debitCardService.toggleOnlineTransactions(cardNumber);
        Map<String, Object> response = new HashMap<>();
        response.put("success", ok);
        response.put("message", ok ? "Online transactions updated" : "Card not found");
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Get card requests
     */
    @GetMapping("/admin/card/requests")
    public ResponseEntity<Map<String, Object>> getCardRequests() {
        List<DebitCard> requests = debitCardService.getPendingRequests();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("requests", requests);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Approve card
     */
    @PostMapping("/admin/card/approve")
    public ResponseEntity<Map<String, Object>> approveCard(@RequestParam Long cardId) {
        boolean ok = debitCardService.approveCard(cardId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", ok);
        response.put("message", ok ? "Card approved" : "Card not found");
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: Reject card
     */
    @PostMapping("/admin/card/reject")
    public ResponseEntity<Map<String, Object>> rejectCard(@RequestParam Long cardId) {
        boolean ok = debitCardService.rejectCard(cardId);
        Map<String, Object> response = new HashMap<>();
        response.put("success", ok);
        response.put("message", ok ? "Card rejected" : "Card not found");
        return ResponseEntity.ok(response);
    }

    /**
     * Change PIN
     */
    @PostMapping("/debit-card/change-pin")
    public ResponseEntity<Map<String, Object>> changePin(@RequestParam String cardNumber, @RequestParam String newPin) {
        // allowing pin change without old pin for simplicity/admin override simulation
        // or simple user reset
        boolean ok = debitCardService.changePin(cardNumber, null, newPin);
        Map<String, Object> response = new HashMap<>();
        response.put("success", ok);
        response.put("message", ok ? "PIN updated successfully" : "Card not found");
        return ResponseEntity.ok(response);
    }

    /**
     * Get debit cards
     */
    @GetMapping("/debit-cards/{accountNumber}")
    public ResponseEntity<Map<String, Object>> getDebitCards(@PathVariable String accountNumber) {
        List<DebitCard> cards = debitCardService.getCards(accountNumber);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cards", cards);
        return ResponseEntity.ok(response);
    }

    /**
     * Block a debit card
     */
    @PostMapping("/debit-card/block")
    public ResponseEntity<Map<String, Object>> blockCard(@RequestParam String cardNumber) {
        boolean ok = debitCardService.blockCard(cardNumber);
        Map<String, Object> response = new HashMap<>();
        response.put("success", ok);
        response.put("message", ok ? "Card blocked" : "Card not found");
        return ResponseEntity.ok(response);
    }

    /**
     * Unblock a debit card
     */
    @PostMapping("/debit-card/unblock")
    public ResponseEntity<Map<String, Object>> unblockCard(@RequestParam String cardNumber) {
        boolean ok = debitCardService.unblockCard(cardNumber);
        Map<String, Object> response = new HashMap<>();
        response.put("success", ok);
        response.put("message", ok ? "Card unblocked" : "Card not found");
        return ResponseEntity.ok(response);
    }

    /**
     * Change daily limit for a card
     */
    @PostMapping("/debit-card/change-limit")
    public ResponseEntity<Map<String, Object>> changeCardLimit(@RequestParam String cardNumber,
            @RequestParam double newLimit) {
        boolean ok = debitCardService.changeDailyLimit(cardNumber, newLimit);
        Map<String, Object> response = new HashMap<>();
        response.put("success", ok);
        response.put("message", ok ? "Limit updated" : "Card not found");
        return ResponseEntity.ok(response);
    }

    /**
     * Request a new cheque book
     */
    @PostMapping("/cheque/request")
    public ResponseEntity<Map<String, Object>> requestChequeBook(@RequestParam String accountNumber) {
        ChequeRequest req = chequeService.requestNewBook(accountNumber);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Cheque book request submitted");
        response.put("requestId", req.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Stop a cheque
     */
    @PostMapping("/cheque/stop")
    public ResponseEntity<Map<String, Object>> stopCheque(@RequestParam String accountNumber,
            @RequestParam String chequeNumber) {
        ChequeRequest req = chequeService.stopCheque(accountNumber, chequeNumber);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Stop cheque request submitted");
        response.put("requestId", req.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * View cheque requests for account
     */
    @GetMapping("/cheque/requests/{accountNumber}")
    public ResponseEntity<Map<String, Object>> listChequeRequests(@PathVariable String accountNumber) {
        List<ChequeRequest> list = chequeService.getRequests(accountNumber);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("requests", list);
        return ResponseEntity.ok(response);
    }

    /**
     * Change account PIN
     */
    @PostMapping("/change-pin")
    public ResponseEntity<Map<String, Object>> changePin(@RequestParam String accountNumber,
            @RequestParam String oldPin, @RequestParam String newPin) {
        boolean ok = accountService.changePin(accountNumber, oldPin, newPin);
        Map<String, Object> response = new HashMap<>();
        response.put("success", ok);
        response.put("message", ok ? "PIN changed successfully" : "Invalid account or old PIN");
        return ResponseEntity.ok(response);
    }

    /**
     * Update profile fields
     */
    @PostMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestParam String accountNumber,
            @RequestParam(required = false) String mobileNumber,
            @RequestParam(required = false) String mailingAddress,
            @RequestParam(required = false) String nomineeName,
            @RequestParam(required = false) String nomineeRelation) {
        boolean ok = accountService.updateProfile(accountNumber, mobileNumber, mailingAddress, nomineeName,
                nomineeRelation);
        Map<String, Object> response = new HashMap<>();
        response.put("success", ok);
        response.put("message", ok ? "Profile updated" : "Account not found");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/daily-limit")
    public ResponseEntity<Map<String, Object>> setDailyLimit(@RequestParam String accountNumber,
            @RequestParam double limit) {
        boolean ok = accountService.updateDailyLimit(accountNumber, limit);
        Map<String, Object> response = new HashMap<>();
        response.put("success", ok);
        response.put("message", ok ? "Daily limit updated" : "Account not found");
        return ResponseEntity.ok(response);
    }

    /**
     * Send OTP for login
     */
    @PostMapping("/auth/login/send-otp")
    public ResponseEntity<Map<String, Object>> sendLoginOtp(@RequestParam String accountNumber) {
        try {
            String message = authService.sendLoginOtp(accountNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Verify OTP for login
     */
    @PostMapping("/auth/login/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyLoginOtp(
            @RequestParam String accountNumber,
            @RequestParam String otp) {
        try {
            boolean isValid = authService.verifyLoginOtp(accountNumber, otp);
            Map<String, Object> response = new HashMap<>();
            response.put("success", isValid);
            response.put("message", "OTP verified successfully. You can now login.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get account holder name by account number
     */
    @GetMapping("/account-holder/{accountNumber}")
    public ResponseEntity<Map<String, Object>> getAccountHolderName(@PathVariable String accountNumber) {
        try {
            Map<String, Object> accountDetails = accountService.getAccountDetails(accountNumber);
            if (accountDetails != null && (Boolean) accountDetails.get("success")) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("accountNumber", accountNumber);
                response.put("accountHolderName", accountDetails.get("accountHolderName"));
                // record a quick lookup in login history as a lightweight audit (success=true)
                try {
                    loginHistoryService.record(accountNumber, null, "account-holder-lookup", true);
                } catch (Exception ignored) {
                }
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Account not found");
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Send OTP for transaction (Withdraw/Transfer/Loan > ₹5000)
     */
    @PostMapping("/auth/transaction/send-otp")
    public ResponseEntity<Map<String, Object>> sendTransactionOtp(
            @RequestParam String accountNumber,
            @RequestParam String transactionType) {
        try {
            String message = authService.sendTransactionOtp(accountNumber, transactionType);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Verify OTP for transaction
     */
    @PostMapping("/auth/transaction/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyTransactionOtp(
            @RequestParam String accountNumber,
            @RequestParam String otp,
            @RequestParam String transactionType) {
        try {
            boolean isValid = authService.verifyTransactionOtp(accountNumber, otp, transactionType);
            Map<String, Object> response = new HashMap<>();
            response.put("success", isValid);
            response.put("message", "OTP verified successfully. Transaction proceeding.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: system statistics (counts and sums)
     */
    /**
     * Admin: system statistics (counts and sums)
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            long totalAccounts = accountService.countAccounts();
            long totalTransactions = accountService.countTransactions();
            long activeUsers = accountService.countActiveUsers();
            double totalBalance = accountService.sumBalances();

            stats.put("success", true);
            stats.put("totalAccounts", totalAccounts);
            stats.put("totalTransactions", totalTransactions);
            stats.put("activeUsers", activeUsers);
            stats.put("totalBalance", totalBalance);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin Analytics Endpoint
     * Combines Account/Transaction stats and Card request stats
     */
    @GetMapping("/admin/analytics")
    public ResponseEntity<Map<String, Object>> getAdminAnalytics() {
        Map<String, Object> stats = accountService.getAdminAnalytics();
        List<Map<String, Object>> cardStats = debitCardService.getCardRequestStats();
        stats.put("cardRequestTrends", cardStats);
        return ResponseEntity.ok(stats);
    }

    /**
     * Admin: list all accounts
     */
    @GetMapping("/admin/accounts")
    public ResponseEntity<Map<String, Object>> listAllAccounts() {
        try {
            java.util.List<Account> accounts = accountService.getAllAccounts();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("accounts", accounts);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: list all transactions
     */
    @GetMapping("/admin/transactions")
    public ResponseEntity<Map<String, Object>> listAllTransactions(@RequestParam(required = false) String search) {
        try {
            java.util.List<Transaction> txns;
            if (search != null && !search.trim().isEmpty()) {
                txns = accountService.searchTransactions(search);
            } else {
                txns = accountService.getAllTransactions();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactions", txns);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: freeze account (user cannot login/transact)
     */
    @PostMapping("/admin/freeze-account")
    public ResponseEntity<Map<String, Object>> freezeAccount(@RequestParam String accountNumber) {
        try {
            boolean ok = accountService.freezeAccount(accountNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", ok);
            response.put("message", ok ? "Account frozen" : "Account not found");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: unfreeze account
     */
    @PostMapping("/admin/unfreeze-account")
    public ResponseEntity<Map<String, Object>> unfreezeAccount(@RequestParam String accountNumber) {
        try {
            boolean ok = accountService.unfreezeAccount(accountNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", ok);
            response.put("message", ok ? "Account unfrozen" : "Account not found");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: Send Message (Individual or Broadcast)
     * type: "INDIVIDUAL" or "BROADCAST"
     */
    @PostMapping("/admin/message/send")
    public ResponseEntity<Map<String, Object>> sendAdminMessage(
            @RequestParam(required = false) String accountNumber,
            @RequestParam String message,
            @RequestParam String type) {

        boolean success = false;
        if ("BROADCAST".equalsIgnoreCase(type)) {
            accountService.sendBroadcastMessage(message);
            success = true;
        } else {
            success = accountService.sendAdminMessage(accountNumber, message);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", success ? "Message sent successfully" : "Failed to send message (User not found?)");
        return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
    }

    /**
     * Admin: Get Message History
     */
    @GetMapping("/admin/message/history")
    public ResponseEntity<Map<String, Object>> getMessageHistory() {
        List<org.example.bankingsystem.model.AdminMessage> history = accountService.getMessageHistory();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("history", history);
        return ResponseEntity.ok(response);
    }

    /**
     * Admin: deactivate account
     */
    @PostMapping("/admin/deactivate-account")
    public ResponseEntity<Map<String, Object>> deactivateAccount(@RequestParam String accountNumber) {
        try {
            boolean ok = accountService.deactivateAccount(accountNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", ok);
            response.put("message", ok ? "Account deactivated" : "Account not found");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: activate account
     */
    @PostMapping("/admin/activate-account")
    public ResponseEntity<Map<String, Object>> activateAccount(@RequestParam String accountNumber) {
        try {
            boolean ok = accountService.activateAccount(accountNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", ok);
            response.put("message", ok ? "Account activated" : "Account not found");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: delete account
     */
    @PostMapping("/admin/delete-account")
    public ResponseEntity<Map<String, Object>> deleteAccountAsAdmin(@RequestParam String accountNumber) {
        try {
            boolean ok = accountService.deleteAccountAsAdmin(accountNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", ok);
            response.put("message", ok ? "Account deleted" : "Account not found");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: Get pending delete requests
     */
    @GetMapping("/admin/delete/requests")
    public ResponseEntity<Map<String, Object>> getDeleteRequests() {
        try {
            List<org.example.bankingsystem.model.DeleteRequest> requests = accountService.getPendingDeleteRequests();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("requests", requests);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: Approve delete request
     */
    @PostMapping("/admin/delete/approve")
    public ResponseEntity<Map<String, Object>> approveDeleteRequest(@RequestParam Long requestId) {
        try {
            boolean ok = accountService.approveDeleteRequest(requestId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", ok);
            response.put("message", ok ? "Request approved and account deleted." : "Failed to approve request.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Admin: Reject delete request
     */
    @PostMapping("/admin/delete/reject")
    public ResponseEntity<Map<String, Object>> rejectDeleteRequest(@RequestParam Long requestId) {
        try {
            boolean ok = accountService.rejectDeleteRequest(requestId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", ok);
            response.put("message", ok ? "Request rejected." : "Failed to reject request.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * User Analytics: Get last 30 days stats
     */
    @GetMapping("/analytics/{accountNumber}")
    public ResponseEntity<Map<String, Object>> getAnalytics(@PathVariable String accountNumber) {
        try {
            Map<String, Object> data = accountService.getAnalyticsData(accountNumber);
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
