package org.example.bankingsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BankingsystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(BankingsystemApplication.class, args);

        System.out.println("✅ Banking System Started Successfully!");
        System.out.println("\n http://localhost:8080/");
    }
}
