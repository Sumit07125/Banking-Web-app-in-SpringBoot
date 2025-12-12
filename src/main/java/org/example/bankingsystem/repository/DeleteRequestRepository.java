package org.example.bankingsystem.repository;

import org.example.bankingsystem.model.DeleteRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DeleteRequestRepository extends JpaRepository<DeleteRequest, Long> {
    List<DeleteRequest> findByStatus(String status);

    Optional<DeleteRequest> findByAccountNumberAndStatus(String accountNumber, String status);

    void deleteByAccountNumber(String accountNumber);
}
