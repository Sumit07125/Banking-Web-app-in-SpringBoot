package org.example.bankingsystem.service;

import org.example.bankingsystem.model.DebitCard;
import org.example.bankingsystem.model.Account;
import org.example.bankingsystem.repository.DebitCardRepository;
import org.example.bankingsystem.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class DebitCardService {

    @Autowired
    private DebitCardRepository debitCardRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private EmailService emailService;

    // Issue debit card for an account
    public DebitCard issueDebitCard(String accountNumber) {
        Optional<Account> account = accountRepository.findById(accountNumber);

        if (!account.isPresent()) {
            throw new RuntimeException("Account not found");
        }

        Account acc = account.get();

        // Check if account already has an active card or pending request
        Optional<DebitCard> existingCard = debitCardRepository.findByAccountNumberAndStatus(accountNumber, "ACTIVE");
        if (existingCard.isPresent()) {
            throw new RuntimeException("Account already has an active debit card");
        }
        if (debitCardRepository.findByAccountNumberAndStatus(accountNumber, "PENDING").isPresent()) {
            throw new RuntimeException("A card request is already pending.");
        }

        DebitCard card = new DebitCard(accountNumber, acc.getName());
        DebitCard savedCard = debitCardRepository.save(card);

        // Send email with card details
        emailService.sendDebitCardEmail(acc.getEmail(), savedCard.getCardNumber(),
                savedCard.getCardHolderName(), savedCard.getCvv(),
                savedCard.getExpiryDate().toString());

        return savedCard;
    }

    // Get all cards for an account
    public List<DebitCard> getCards(String accountNumber) {
        return debitCardRepository.findByAccountNumber(accountNumber);
    }

    // Get active card for an account
    public Optional<DebitCard> getActiveCard(String accountNumber) {
        return debitCardRepository.findByAccountNumberAndStatus(accountNumber, "ACTIVE");
    }

    // Block a card
    public boolean blockCard(String cardNumber) {
        Optional<DebitCard> card = debitCardRepository.findByCardNumber(cardNumber);

        if (!card.isPresent()) {
            return false;
        }

        DebitCard cardObj = card.get();
        cardObj.setStatus("BLOCKED");
        debitCardRepository.save(cardObj);
        return true;
    }

    // Unblock a card
    public boolean unblockCard(String cardNumber) {
        Optional<DebitCard> card = debitCardRepository.findByCardNumber(cardNumber);

        if (!card.isPresent()) {
            return false;
        }

        DebitCard cardObj = card.get();
        cardObj.setStatus("ACTIVE");
        debitCardRepository.save(cardObj);
        return true;
    }

    // Check if daily limit is available
    public boolean isDailyLimitAvailable(String cardNumber, double amount) {
        Optional<DebitCard> card = debitCardRepository.findByCardNumber(cardNumber);

        if (!card.isPresent()) {
            return false;
        }

        DebitCard cardObj = card.get();

        // Reset daily spending if date has changed
        if (!cardObj.getLastResetDate().equals(LocalDate.now())) {
            cardObj.setCurrentDaySpent(0);
            cardObj.setLastResetDate(LocalDate.now());
            debitCardRepository.save(cardObj);
        }

        return (cardObj.getCurrentDaySpent() + amount) <= cardObj.getDailyLimit();
    }

    // Update daily spending
    public void updateDailySpending(String cardNumber, double amount) {
        Optional<DebitCard> card = debitCardRepository.findByCardNumber(cardNumber);

        if (card.isPresent()) {
            DebitCard cardObj = card.get();

            // Reset if date changed
            if (!cardObj.getLastResetDate().equals(LocalDate.now())) {
                cardObj.setCurrentDaySpent(0);
                cardObj.setLastResetDate(LocalDate.now());
            }

            cardObj.setCurrentDaySpent(cardObj.getCurrentDaySpent() + amount);
            debitCardRepository.save(cardObj);
        }
    }

    // Change daily limit
    public boolean changeDailyLimit(String cardNumber, double newLimit) {
        Optional<DebitCard> card = debitCardRepository.findByCardNumber(cardNumber);

        if (!card.isPresent()) {
            return false;
        }

        DebitCard cardObj = card.get();
        cardObj.setDailyLimit(newLimit);
        debitCardRepository.save(cardObj);
        return true;
    }

    // Toggle Online Transactions
    public boolean toggleOnlineTransactions(String cardNumber) {
        Optional<DebitCard> card = debitCardRepository.findByCardNumber(cardNumber);
        if (card.isEmpty())
            return false;
        DebitCard c = card.get();
        c.setOnlineTransactionsEnabled(!c.isOnlineTransactionsEnabled());
        debitCardRepository.save(c);
        return true;
    }

    // Change PIN
    public boolean changePin(String cardNumber, String oldPin, String newPin) {
        Optional<DebitCard> card = debitCardRepository.findByCardNumber(cardNumber);
        if (card.isEmpty())
            return false;
        DebitCard c = card.get();
        // If oldPin provided, verify it (assuming we force check, but initial might be
        // default)
        // For simplicity allow reset without old if admin or override needed, but
        // strictly:
        if (c.getPin() != null && !c.getPin().equals(oldPin)) {
            return false;
        }
        c.setPin(newPin);
        debitCardRepository.save(c);
        return true;
    }

    // Admin: Get Pending Requests
    public List<DebitCard> getPendingRequests() {
        return debitCardRepository.findAll().stream().filter(c -> "PENDING".equals(c.getStatus()))
                .collect(java.util.stream.Collectors.toList());
    }

    // Admin: Approve Card
    public boolean approveCard(Long cardId) {
        Optional<DebitCard> opt = debitCardRepository.findById(cardId);
        if (opt.isEmpty())
            return false;
        DebitCard c = opt.get();
        c.setStatus("ACTIVE");
        debitCardRepository.save(c);
        // Send email now? Or assumed sent on creation? Best to send "Approved" email.
        return true;
    }

    // Admin: Reject Card
    public boolean rejectCard(Long cardId) {
        Optional<DebitCard> opt = debitCardRepository.findById(cardId);
        if (opt.isEmpty())
            return false;
        DebitCard c = opt.get();
        c.setStatus("REJECTED");
        debitCardRepository.save(c);
        return true;
    }

    // Analytics: Daily Card Requests (issued date)
    public java.util.List<java.util.Map<String, Object>> getCardRequestStats() {
        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        List<DebitCard> allCards = debitCardRepository.findAll();

        java.util.Map<java.time.LocalDate, Long> dailyRequests = allCards.stream()
                .filter(c -> c.getIssuedDate() != null && c.getIssuedDate().isAfter(thirtyDaysAgo))
                .collect(java.util.stream.Collectors.groupingBy(
                        c -> c.getIssuedDate().toLocalDate(),
                        java.util.stream.Collectors.counting()));

        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (int i = 29; i >= 0; i--) {
            java.time.LocalDate d = java.time.LocalDate.now().minusDays(i);
            java.util.Map<String, Object> point = new java.util.HashMap<>();
            point.put("date", d.toString());
            point.put("requests", dailyRequests.getOrDefault(d, 0L));
            result.add(point);
        }
        return result;
    }
}
