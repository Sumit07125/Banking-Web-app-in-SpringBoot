package org.example.bankingsystem.controller;

import org.example.bankingsystem.model.HelpRequest;
import org.example.bankingsystem.service.HelpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/help")
@CrossOrigin(origins = "http://localhost:3000")
public class RestHelpController {

    @Autowired
    private HelpService helpService;

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitRequest(
            @RequestParam String accountNumber,
            @RequestParam String requestType,
            @RequestParam String message,
            @RequestParam(required = false) String transactionId) {
        try {
            HelpRequest request = helpService.createRequest(accountNumber, requestType, message, transactionId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Request submitted successfully");
            response.put("request", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/requests/{accountNumber}")
    public ResponseEntity<Map<String, Object>> getRequests(@PathVariable String accountNumber) {
        List<HelpRequest> requests = helpService.getRequests(accountNumber);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("requests", requests);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/all")
    public ResponseEntity<Map<String, Object>> getAllRequests() {
        List<HelpRequest> requests = helpService.getAllRequests();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("requests", requests);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/update-status/{id}")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            HelpRequest request = helpService.updateRequestStatus(id, status);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Status updated successfully");
            response.put("request", request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
