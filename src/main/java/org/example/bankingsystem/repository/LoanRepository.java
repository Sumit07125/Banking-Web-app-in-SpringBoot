package org.example.bankingsystem.repository;

import org.example.bankingsystem.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByAccountNumber(String accountNumber);

    Optional<Loan> findByLoanId(String loanId);

    List<Loan> findByAccountNumberAndStatus(String accountNumber, String status);

    List<Loan> findByStatus(String status);
}
