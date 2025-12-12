package org.example.bankingsystem.controller;

import org.example.bankingsystem.service.LoginHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/login-history")
@CrossOrigin(origins = "http://localhost:3000")
public class LoginHistoryController {

    @Autowired
    private LoginHistoryService loginHistoryService;

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Map<String, Object>> getHistory(@PathVariable String accountNumber) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("history", loginHistoryService.getForAccount(accountNumber));
        return ResponseEntity.ok(response);
    }
}
