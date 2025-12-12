package org.example.bankingsystem.repository;

import org.example.bankingsystem.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    java.util.List<Account> findByNameContainingIgnoreCase(String name);

    Optional<Account> findByAccountNumber(String accountNumber);
}
