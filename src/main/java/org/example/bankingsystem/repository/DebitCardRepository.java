package org.example.bankingsystem.repository;

import org.example.bankingsystem.model.DebitCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DebitCardRepository extends JpaRepository<DebitCard, Long> {
    List<DebitCard> findByAccountNumber(String accountNumber);

    Optional<DebitCard> findByCardNumber(String cardNumber);

    Optional<DebitCard> findByAccountNumberAndStatus(String accountNumber, String status);
}
