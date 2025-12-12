package org.example.bankingsystem.service;

import org.example.bankingsystem.model.Account;
import org.example.bankingsystem.model.OtpRequest;
import org.example.bankingsystem.repository.AccountRepository;
import org.example.bankingsystem.repository.OtpRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private OtpRequestRepository otpRequestRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Generate and send OTP for login
     */
    public String sendLoginOtp(String accountNumber) {
        // Check if account exists
        Optional<Account> account = accountRepository.findByAccountNumber(accountNumber);
        if (account.isEmpty()) {
            throw new RuntimeException("Account not found!");
        }

        // Generate 6-digit OTP
        String otp = generateOtp();

        // Save OTP request
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setAccountNumber(accountNumber);
        otpRequest.setOtp(otp);
        otpRequest.setType("LOGIN");
        otpRequest.setCreatedAt(LocalDateTime.now());
        otpRequest.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRequest.setUsed(false);
        otpRequestRepository.save(otpRequest);

        // Send OTP via email
        String email = account.get().getEmail();
        emailService.sendOtpEmail(email, otp, "LOGIN");

        return "OTP sent to " + email + ". Valid for 10 minutes.";
    }

    /**
     * Verify login OTP
     */
    public boolean verifyLoginOtp(String accountNumber, String otp) {
        // Find the latest OTP request
        java.util.List<OtpRequest> otpRequests = otpRequestRepository.findByAccountNumberAndType(accountNumber,
                "LOGIN");

        if (otpRequests.isEmpty()) {
            throw new RuntimeException("No OTP request found!");
        }

        // Get the latest OTP
        OtpRequest latestOtp = otpRequests.get(otpRequests.size() - 1);

        // Check if OTP is expired
        if (LocalDateTime.now().isAfter(latestOtp.getExpiresAt())) {
            throw new RuntimeException("OTP has expired!");
        }

        // Check if OTP is already used
        if (latestOtp.isUsed()) {
            throw new RuntimeException("OTP has already been used!");
        }

        // Verify OTP
        if (!latestOtp.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP!");
        }

        // Mark OTP as used
        latestOtp.setUsed(true);
        otpRequestRepository.save(latestOtp);

        return true;
    }

    /**
     * Generate 6-digit random OTP
     */
    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    /**
     * Send transaction OTP (for withdrawals, transfers, loans > ₹5000)
     */
    public String sendTransactionOtp(String accountNumber, String transactionType) {
        // Check if account exists
        Optional<Account> account = accountRepository.findByAccountNumber(accountNumber);
        if (account.isEmpty()) {
            throw new RuntimeException("Account not found!");
        }

        // Generate 6-digit OTP
        String otp = generateOtp();

        // Save OTP request
        OtpRequest otpRequest = new OtpRequest();
        otpRequest.setAccountNumber(accountNumber);
        otpRequest.setOtp(otp);
        otpRequest.setType(transactionType);
        otpRequest.setCreatedAt(LocalDateTime.now());
        otpRequest.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRequest.setUsed(false);
        otpRequestRepository.save(otpRequest);

        // Send OTP via email
        String email = account.get().getEmail();
        emailService.sendOtpEmail(email, otp, transactionType);

        return "OTP sent to " + email + ". Valid for 10 minutes.";
    }

    /**
     * Verify transaction OTP
     */
    public boolean verifyTransactionOtp(String accountNumber, String otp, String transactionType) {
        // Find the latest OTP request
        java.util.List<OtpRequest> otpRequests = otpRequestRepository.findByAccountNumberAndType(accountNumber,
                transactionType);

        if (otpRequests.isEmpty()) {
            throw new RuntimeException("No OTP request found!");
        }

        // Get the latest OTP
        OtpRequest latestOtp = otpRequests.get(otpRequests.size() - 1);

        // Check if OTP is expired
        if (LocalDateTime.now().isAfter(latestOtp.getExpiresAt())) {
            throw new RuntimeException("OTP has expired!");
        }

        // Check if OTP is already used
        if (latestOtp.isUsed()) {
            throw new RuntimeException("OTP has already been used!");
        }

        // Verify OTP
        if (!latestOtp.getOtp().equals(otp)) {
            throw new RuntimeException("Invalid OTP!");
        }

        // Mark OTP as used
        latestOtp.setUsed(true);
        otpRequestRepository.save(latestOtp);

        return true;
    }

    // Check if a transaction is verified (delegates to OtpService)
    public boolean isTransactionVerified(String accountNumber, String type) {
        // We need to inject OtpService here or implement logic directly.
        // Since OtpService has the logic, let's use it. But AuthService doesn't have
        // OtpService injected.
        // Let's implement the logic here directly as it's cleaner than circular
        // dependency.

        java.util.List<OtpRequest> requests = otpRequestRepository.findByAccountNumberAndType(accountNumber, type);
        if (requests.isEmpty()) {
            return false;
        }
        // Get latest
        OtpRequest latest = requests.get(requests.size() - 1);

        // Check if used and not expired (allow 5 min buffer after verification)
        if (latest.isUsed() && latest.getExpiresAt().isAfter(LocalDateTime.now().minusMinutes(5))) {
            return true;
        }
        return false;
    }
}
