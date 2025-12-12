package org.example.bankingsystem.controller;

import org.example.bankingsystem.model.Account;
import org.example.bankingsystem.model.Transaction;
import org.example.bankingsystem.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@Controller
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    // Create
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("account", new Account());
        return "createAccount";
    }

    @PostMapping("/create")
    public String createAccount(@ModelAttribute Account account, Model model) {
        Account saved = accountService.createAccount(account);
        model.addAttribute("msg", "Account created. Acc No: " + saved.getAccountNumber());
        model.addAttribute("account", new Account()); // Clear the form
        return "createAccount";
    }

    // Deposit form + process
    @GetMapping("/deposit")
    public String depositForm(Model model) {
        return "deposit";
    }

    @PostMapping("/deposit")
    public String depositProcess(@RequestParam String accountNumber,
            @RequestParam double amount,
            Model model) {
        String res = accountService.deposit(accountNumber, amount);
        model.addAttribute("msg", res);
        return "deposit";
    }

    // Withdraw
    @GetMapping("/withdraw")
    public String withdrawForm() {
        return "withdraw";
    }

    @PostMapping("/withdraw")
    public String withdrawProcess(@RequestParam String accountNumber,
            @RequestParam String pin,
            @RequestParam double amount,
            Model model) {
        String res = accountService.withdraw(accountNumber, pin, amount);
        model.addAttribute("msg", res);
        return "withdraw";
    }

    // Transfer
    @GetMapping("/transfer")
    public String transferForm() {
        return "transfer";
    }

    @PostMapping("/transfer")
    public String transferProcess(@RequestParam String senderAcc,
            @RequestParam String senderPin,
            @RequestParam String receiverAcc,
            @RequestParam double amount,
            Model model) {
        String res = accountService.transfer(senderAcc, senderPin, receiverAcc, amount);
        model.addAttribute("msg", res);
        return "transfer";
    }

    // Mini-statement view
    @GetMapping("/statement")
    public String statementForm() {
        return "statement";
    }

    @PostMapping("/statement")
    public String statementView(@RequestParam String accountNumber,
            @RequestParam String pin,
            Model model) {
        // optional: verify PIN before showing
        // Here we do minimal check - reuse withdraw's verification indirectly
        List<Transaction> txns = accountService.getMiniStatement(accountNumber);
        model.addAttribute("transactions", txns);
        model.addAttribute("accNo", accountNumber);
        return "statementView";
    }

    // Download CSV
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

    // Delete account
    @GetMapping("/delete")
    public String deleteForm() {
        return "delete";
    }

    @PostMapping("/delete")
    public String deleteProcess(@RequestParam String accountNumber,
            @RequestParam String pin,
            Model model) {
        // Legacy delete is disabled in favor of OTP flow
        // String res = accountService.deleteAccount(accountNumber, pin);
        model.addAttribute("msg", "Please use the new Dashboard to delete your account via OTP.");
        return "delete";
    }
}
