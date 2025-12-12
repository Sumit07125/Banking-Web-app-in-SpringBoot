package org.example.bankingsystem.repository;

import org.example.bankingsystem.model.OtpRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OtpRequestRepository extends JpaRepository<OtpRequest, Long> {
    Optional<OtpRequest> findByAccountNumberAndOtpAndUsedFalse(String accountNumber, String otp);

    Optional<OtpRequest> findByAccountNumberAndTypeAndUsedFalse(String accountNumber, String type);

    List<OtpRequest> findByAccountNumberAndCreatedAtAfter(String accountNumber, LocalDateTime createdAfter);

    List<OtpRequest> findByAccountNumberAndType(String accountNumber, String type);
}
