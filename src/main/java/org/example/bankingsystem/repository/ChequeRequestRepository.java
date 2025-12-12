package org.example.bankingsystem.repository;

import org.example.bankingsystem.model.ChequeRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChequeRequestRepository extends JpaRepository<ChequeRequest, Long> {
    List<ChequeRequest> findByAccountNumberOrderByCreatedAtDesc(String accountNumber);
}
