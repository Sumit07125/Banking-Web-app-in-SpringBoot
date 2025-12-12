package org.example.bankingsystem.service;

import org.example.bankingsystem.model.HelpRequest;
import org.example.bankingsystem.repository.HelpRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HelpService {

    @Autowired
    private HelpRequestRepository helpRequestRepository;

    public HelpRequest createRequest(String accountNumber, String requestType, String message, String transactionId) {
        HelpRequest request = new HelpRequest(accountNumber, requestType, message, transactionId);
        return helpRequestRepository.save(request);
    }

    public List<HelpRequest> getRequests(String accountNumber) {
        return helpRequestRepository.findByAccountNumber(accountNumber);
    }

    public List<HelpRequest> getAllRequests() {
        return helpRequestRepository.findAll(org.springframework.data.domain.Sort
                .by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
    }

    public HelpRequest updateRequestStatus(Long id, String status) {
        HelpRequest request = helpRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(status);
        return helpRequestRepository.save(request);
    }
}
