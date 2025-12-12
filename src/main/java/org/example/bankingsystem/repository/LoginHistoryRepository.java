package org.example.bankingsystem.repository;

import org.example.bankingsystem.model.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
    List<LoginHistory> findByAccountNumberOrderByLoginTimeDesc(String accountNumber);
}
