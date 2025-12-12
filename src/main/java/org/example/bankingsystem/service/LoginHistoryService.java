package org.example.bankingsystem.service;

import org.example.bankingsystem.model.LoginHistory;
import org.example.bankingsystem.repository.LoginHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LoginHistoryService {

    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

    public LoginHistory record(String accountNumber, String ipAddress, String userAgent, boolean success) {
        LoginHistory h = new LoginHistory(accountNumber, ipAddress, userAgent, success);
        return loginHistoryRepository.save(h);
    }

    public List<LoginHistory> getForAccount(String accountNumber) {
        return loginHistoryRepository.findByAccountNumberOrderByLoginTimeDesc(accountNumber);
    }
}
