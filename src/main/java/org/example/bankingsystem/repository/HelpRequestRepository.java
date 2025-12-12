package org.example.bankingsystem.repository;

import org.example.bankingsystem.model.HelpRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HelpRequestRepository extends JpaRepository<HelpRequest, Long> {
    List<HelpRequest> findByAccountNumber(String accountNumber);
}
