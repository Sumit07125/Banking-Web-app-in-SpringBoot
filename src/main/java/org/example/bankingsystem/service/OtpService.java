package org.example.bankingsystem.service;

import org.example.bankingsystem.model.OtpRequest;
import org.example.bankingsystem.repository.OtpRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpRequestRepository otpRequestRepository;

    @Autowired
    private EmailService emailService;

    // Generate a 6-digit OTP
    public String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Send OTP to email
    public void sendOtp(String accountNumber, String email, String type) {
        String otp = generateOtp();
        OtpRequest otpRequest = new OtpRequest(accountNumber, otp, type);
        otpRequestRepository.save(otpRequest);
        emailService.sendOtpEmail(email, otp, type);
    }

    // Verify OTP
    public boolean verifyOtp(String accountNumber, String otp) {
        Optional<OtpRequest> otpRequest = otpRequestRepository.findByAccountNumberAndOtpAndUsedFalse(accountNumber,
                otp);

        if (otpRequest.isPresent()) {
            OtpRequest otp_obj = otpRequest.get();

            // Check if OTP has expired (10 minutes)
            if (otp_obj.getExpiresAt().isBefore(LocalDateTime.now())) {
                return false;
            }

            // Mark OTP as used
            otp_obj.setUsed(true);
            otpRequestRepository.save(otp_obj);
            return true;
        }
        return false;
    }

    // Check if OTP is required
    public boolean isOtpRequired(String accountNumber, String type) {
        Optional<OtpRequest> otpRequest = otpRequestRepository.findByAccountNumberAndTypeAndUsedFalse(accountNumber,
                type);
        return otpRequest.isPresent();
    }

    // Check if a transaction is verified (OTP used recently)
    public boolean isTransactionVerified(String accountNumber, String type) {
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
