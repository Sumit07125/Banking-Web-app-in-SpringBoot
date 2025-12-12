package org.example.bankingsystem.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.example.bankingsystem.service.EmailService;

/**
 * Optional Test Utility for Email Service
 * Set ENABLED = true to run email tests on startup
 * Set ENABLED = false to disable (default for production)
 */
@Component
public class EmailTestUtil implements CommandLineRunner {

    @Autowired
    private EmailService emailService;

    // Set to true to run email tests on startup
    private static final boolean ENABLED = false;

    // Change this to your email address for testing
    private static final String TEST_EMAIL = "sumitmali07125@gmail.com";

    @Override
    public void run(String... args) throws Exception {
        if (!ENABLED) {
            System.out.println("ℹ️ EmailTestUtil is disabled. Set ENABLED = true to run tests.");
            return;
        }

        System.out.println("\n" + "=".repeat(60));
        System.out.println("🧪 EMAIL SERVICE TEST UTILITY");
        System.out.println("=".repeat(60));

        try {
            // Test 1: Plain text email
            System.out.println("\n📧 Test 1: Sending plain text email...");
            emailService.sendEmail(
                    TEST_EMAIL,
                    "Test Email - Plain Text",
                    "This is a plain text test email from Banking System.\n" +
                            "If you received this, the email service is working correctly.\n" +
                            "Test sent at: " + java.time.LocalDateTime.now());
            Thread.sleep(2000); // Wait 2 seconds between emails

            // Test 2: HTML email
            System.out.println("\n📧 Test 2: Sending HTML email...");
            emailService.sendHtmlEmail(
                    TEST_EMAIL,
                    "Test Email - HTML Format",
                    "<html><body style='font-family: Arial;'>" +
                            "<h2>Banking System - HTML Email Test</h2>" +
                            "<p>If you received this email, the HTML email service is working!</p>" +
                            "<p><strong>Test Details:</strong></p>" +
                            "<ul>" +
                            "<li>Time: " + java.time.LocalDateTime.now() + "</li>" +
                            "<li>Service: EmailService (Brevo SMTP)</li>" +
                            "<li>Status: ✅ Working</li>" +
                            "</ul>" +
                            "<hr/>" +
                            "<p style='color: #7f8c8d; font-size: 12px;'>This is an automated test email.</p>" +
                            "</body></html>");
            Thread.sleep(2000);

            // Test 3: OTP Email (LOGIN)
            System.out.println("\n📧 Test 3: Sending OTP email (LOGIN)...");
            emailService.sendOtpEmail(TEST_EMAIL, "123456", "LOGIN");
            Thread.sleep(2000);

            // Test 4: OTP Email (WITHDRAWAL)
            System.out.println("\n📧 Test 4: Sending OTP email (WITHDRAWAL)...");
            emailService.sendOtpEmail(TEST_EMAIL, "654321", "WITHDRAWAL");
            Thread.sleep(2000);

            // Test 5: OTP Email (TRANSFER)
            System.out.println("\n📧 Test 5: Sending OTP email (TRANSFER)...");
            emailService.sendOtpEmail(TEST_EMAIL, "789012", "TRANSFER");

            System.out.println("\n" + "=".repeat(60));
            System.out.println("✅ EMAIL TESTS COMPLETED");
            System.out.println("=".repeat(60));
            System.out.println("📬 Check your email inbox for test messages!");
            System.out.println("ℹ️ Emails should arrive within 1-5 seconds.");
            System.out.println("=".repeat(60) + "\n");

        } catch (Exception e) {
            System.out.println("\n❌ EMAIL TEST FAILED");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
