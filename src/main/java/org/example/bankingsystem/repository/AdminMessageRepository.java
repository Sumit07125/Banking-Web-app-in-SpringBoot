package org.example.bankingsystem.repository;

import org.example.bankingsystem.model.AdminMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminMessageRepository extends JpaRepository<AdminMessage, Long> {
    List<AdminMessage> findAllByOrderBySentAtDesc();
}
