package org.example.bankingsystem.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;

@Service
public class EmailService {

    private final String bankOfficialEmail = "sumitmali07125@gmail.com";

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Send plain text email
     */
    public void sendEmail(String to, String subject, String text) {
        try {
            if (to == null || to.isEmpty()) {
                System.out.println("❌ Email address is null or empty!");
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(bankOfficialEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            System.out.println("✅ Email sent successfully to " + to + " via Gmail SMTP");
        } catch (Exception ex) {
            System.out.println("❌ Email failed to send to " + to + ": " + ex.getMessage());
            System.out.println("❌ Error Type: " + ex.getClass().getName());
            ex.printStackTrace();
        }
    }

    /**
     * Send HTML email (for better formatting) - wrapper for internal method
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        sendHtmlEmailInternal(to, subject, htmlContent);
    }

    /**
     * Internal method to send HTML email, returns true if successful
     */
    private boolean sendHtmlEmailInternal(String to, String subject, String htmlContent) {
        try {
            if (to == null || to.isEmpty()) {
                System.out.println("❌ Email address is null or empty!");
                return false;
            }

            System.out.println("📧 Preparing to send email to: " + to);
            System.out.println("📧 Subject: " + subject);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(bankOfficialEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("✅ HTML Email sent successfully to " + to + " via Gmail SMTP");
            return true;
        } catch (MessagingException ex) {
            System.out.println("❌ MessagingException - Email failed to send to " + to + ": " + ex.getMessage());
            System.out.println("❌ Root Cause: " + (ex.getCause() != null ? ex.getCause().getMessage() : "Unknown"));
            ex.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.out.println("❌ Email failed to send to " + to + ": " + ex.getMessage());
            System.out.println("❌ Error Type: " + ex.getClass().getName());
            ex.printStackTrace();
            return false;
        }
    }

    // Send OTP email with retry logic and exception propagation
    public void sendOtpEmail(String to, String otp, String type) {
        String subject = "Your Banking OTP for " + type;
        String htmlContent = "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Your Banking OTP</h2>" +
                "<p>Dear Customer,</p>" +
                "<p>Your One-Time Password (OTP) for <strong>" + type + "</strong> is:</p>" +
                "<h1 style='color: #2c3e50; background: #ecf0f1; padding: 10px; border-radius: 5px; text-align: center;'>"
                + otp + "</h1>" +
                "<p><strong>⏰ This OTP is valid for 10 minutes.</strong></p>" +
                "<p style='color: red;'>⚠️ Do not share this OTP with anyone.</p>" +
                "<hr/>" +
                "<p>Best regards,<br/><strong>Banking System Team</strong></p>" +
                "<p style='font-size: 12px; color: #7f8c8d;'>Bank Official Email: " + bankOfficialEmail + "</p>" +
                "</body>" +
                "</html>";

        // Retry logic
        int maxRetries = 3;
        boolean sent = false;
        for (int i = 0; i < maxRetries; i++) {
            if (sendHtmlEmailInternal(to, subject, htmlContent)) {
                sent = true;
                break;
            }
            System.out.println("⚠️ Retry " + (i + 1) + "/" + maxRetries + " failed. Waiting 1s...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (!sent) {
            throw new RuntimeException("Failed to send OTP email after " + maxRetries
                    + " attempts. Please check your network connection.");
        }
    }

    // Send account creation confirmation
    public void sendAccountCreationEmail(String to, String accountNumber, String name) {
        String subject = "Account Created Successfully";
        String htmlContent = "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Welcome to Our Banking System!</h2>" +
                "<p>Dear " + name + ",</p>" +
                "<p>Congratulations! Your account has been created successfully.</p>" +
                "<div style='background: #f8f9fa; padding: 15px; border-radius: 5px;'>" +
                "<p><strong>Account Details:</strong></p>" +
                "<p>Account Number: <strong>" + accountNumber + "</strong></p>" +
                "<p>Account Holder: <strong>" + name + "</strong></p>" +
                "</div>" +
                "<p>You can now login with your account number and PIN.</p>" +
                "<hr/>" +
                "<p>Best regards,<br/><strong>Banking System Team</strong></p>" +
                "<p style='font-size: 12px; color: #7f8c8d;'>Bank Official Email: " + bankOfficialEmail + "</p>" +
                "</body>" +
                "</html>";
        sendHtmlEmail(to, subject, htmlContent);
    }

    // Send transaction notification
    public void sendTransactionEmail(String to, String accountNumber, String type, double amount, double balanceAfter,
            String transactionId) {
        String subject = "Transaction Notification - " + type;
        String htmlContent = "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Transaction Confirmation</h2>" +
                "<p>Dear Customer,</p>" +
                "<p>A <strong>" + type + "</strong> transaction has been processed on your account.</p>" +
                "<div style='background: #f8f9fa; padding: 15px; border-radius: 5px;'>" +
                "<p><strong>Transaction Details:</strong></p>" +
                "<p>Account Number: <strong>" + accountNumber + "</strong></p>" +
                "<p>Transaction Type: <strong>" + type + "</strong></p>" +
                "<p>Amount: <strong style='color: green;'>₹" + amount + "</strong></p>" +
                "<p>Balance After: <strong>₹" + balanceAfter + "</strong></p>" +
                "<p>Transaction ID: <strong>" + transactionId + "</strong></p>" +
                "<p>Date & Time: <strong>" + LocalDateTime.now() + "</strong></p>" +
                "</div>" +
                "<p style='color: red;'><strong>⚠️ If you did not authorize this transaction, please contact us immediately.</strong></p>"
                +
                "<hr/>" +
                "<p>Best regards,<br/><strong>Banking System Team</strong></p>" +
                "<p style='font-size: 12px; color: #7f8c8d;'>Bank Official Email: " + bankOfficialEmail + "</p>" +
                "</body>" +
                "</html>";
        sendHtmlEmail(to, subject, htmlContent);
    }

    // Send loan disbursement email
    public void sendLoanEmail(String to, String accountNumber, double loanAmount, int durationMonths, double monthlyEmi,
            double rateOfInterest, String loanId) {
        String subject = "Loan Disbursed - " + loanId;
        String htmlContent = "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Loan Approved & Disbursed</h2>" +
                "<p>Dear Customer,</p>" +
                "<p>Your loan has been approved and disbursed successfully.</p>" +
                "<div style='background: #f8f9fa; padding: 15px; border-radius: 5px;'>" +
                "<p><strong>Loan Details:</strong></p>" +
                "<p>Account Number: <strong>" + accountNumber + "</strong></p>" +
                "<p>Loan ID: <strong>" + loanId + "</strong></p>" +
                "<p>Loan Amount: <strong style='color: green;'>₹" + loanAmount + "</strong></p>" +
                "<p>Duration: <strong>" + durationMonths + " months</strong></p>" +
                "<p>Monthly EMI: <strong>₹" + String.format("%.2f", monthlyEmi) + "</strong></p>" +
                "<p>Rate of Interest: <strong>" + rateOfInterest + "% per annum</strong></p>" +
                "</div>" +
                "<p>The EMI will be automatically deducted from your account on the 1st of every month.</p>" +
                "<hr/>" +
                "<p>Best regards,<br/><strong>Banking System Team</strong></p>" +
                "<p style='font-size: 12px; color: #7f8c8d;'>Bank Official Email: " + bankOfficialEmail + "</p>" +
                "</body>" +
                "</html>";
        sendHtmlEmail(to, subject, htmlContent);
    }

    // Send debit card issuance email
    public void sendDebitCardEmail(String to, String cardNumber, String cardHolderName, String cvv, String expiryDate) {
        String subject = "Your Debit Card has been Issued";
        String htmlContent = "<html>" +
                "<body style='font-family: Arial, sans-serif;'>" +
                "<h2>Debit Card Issued Successfully</h2>" +
                "<p>Dear " + cardHolderName + ",</p>" +
                "<p>Your debit card has been issued successfully.</p>" +
                "<div style='background: #f8f9fa; padding: 15px; border-radius: 5px;'>" +
                "<p><strong>Card Details:</strong></p>" +
                "<p>Card Number: <strong>" + cardNumber + "</strong></p>" +
                "<p>Card Holder: <strong>" + cardHolderName + "</strong></p>" +
                "<p>CVV: <strong>" + cvv + "</strong></p>" +
                "<p>Expiry Date: <strong>" + expiryDate + "</strong></p>" +
                "</div>" +
                "<p style='color: red;'><strong>🔒 Please keep your CVV safe and never share it with anyone.</strong></p>"
                +
                "<p><strong>💳 Daily Limit: ₹50,000</strong></p>" +
                "<hr/>" +
                "<p>Best regards,<br/><strong>Banking System Team</strong></p>" +
                "<p style='font-size: 12px; color: #7f8c8d;'>Bank Official Email: " + bankOfficialEmail + "</p>" +
                "</body>" +
                "</html>";
        sendHtmlEmail(to, subject, htmlContent);
    }

    // Send email with attachment
    public void sendEmailWithAttachment(String to, String subject, String text, byte[] attachmentData,
            String attachmentName) {
        try {
            if (to == null || to.isEmpty()) {
                System.out.println("❌ Email address is null or empty!");
                return;
            }

            MimeMessage message = mailSender.createMimeMessage();
            // Use true for multipart message
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(bankOfficialEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(text, true);

            // Add attachment
            if (attachmentData != null && attachmentData.length > 0) {
                helper.addAttachment(attachmentName,
                        new jakarta.mail.util.ByteArrayDataSource(attachmentData, "application/pdf"));
            }

            mailSender.send(message);
            System.out.println("✅ Email with attachment sent successfully to " + to);
        } catch (MessagingException ex) {
            System.out.println("❌ MessagingException - Email failed to send to " + to + ": " + ex.getMessage());
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println("❌ Email failed to send to " + to + ": " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
