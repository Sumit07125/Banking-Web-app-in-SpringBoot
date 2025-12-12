package org.example.bankingsystem.service;

import org.example.bankingsystem.model.ChequeRequest;
import org.example.bankingsystem.repository.ChequeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChequeService {

    @Autowired
    private ChequeRequestRepository chequeRequestRepository;

    public ChequeRequest requestNewBook(String accountNumber) {
        ChequeRequest req = new ChequeRequest(accountNumber, "NEW_BOOK", null);
        req.setStatus("PROCESSING");
        return chequeRequestRepository.save(req);
    }

    public ChequeRequest stopCheque(String accountNumber, String chequeNumber) {
        ChequeRequest req = new ChequeRequest(accountNumber, "STOP_PAYMENT", chequeNumber);
        req.setStatus("COMPLETED");
        return chequeRequestRepository.save(req);
    }

    public List<ChequeRequest> getRequests(String accountNumber) {
        return chequeRequestRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber);
    }
}
