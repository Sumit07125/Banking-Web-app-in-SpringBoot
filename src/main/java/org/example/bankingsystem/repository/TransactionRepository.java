package org.example.bankingsystem.repository;

import org.example.bankingsystem.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
        List<Transaction> findByAccountNumberOrderByDateDesc(String accountNumber);

        @org.springframework.data.jpa.repository.Query("SELECT t FROM Transaction t WHERE " +
                        "LOWER(t.transactionId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(t.accountNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "t.accountNumber IN :accountNumbers")
        List<Transaction> searchTransactions(@org.springframework.data.repository.query.Param("query") String query,
                        @org.springframework.data.repository.query.Param("accountNumbers") List<String> accountNumbers);

        List<Transaction> findByAccountNumberAndDateBetween(String accountNumber, java.time.LocalDateTime start,
                        java.time.LocalDateTime end);
}
