package com.geekbank.bank.services;

import com.geekbank.bank.models.*;
import com.geekbank.bank.repositories.AccountRepository;
import com.geekbank.bank.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final SendGridEmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, AccountService accountService, AccountRepository accountRepository, SendGridEmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountService = accountService;
        this.accountRepository = accountRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> getUserById(long userId) {
        return userRepository.findById(userId);
    }

    public void registerUser(User user) {

        user.setEnabled(false);
        String token = UUID.randomUUID().toString();
        user.setActivationToken(token);
        userRepository.save(user);


        Account account = new Account();
        account.setUser(user);
        account.setCurrency("USD");
        account.setStatus(AccountStatus.ACTIVE);
        account.setVerificationStatus(VerificationStatus.UNVERIFIED);
        account.setBalance(0.0);
        account.setLoyaltyPoints(0);
        account.setDailyLimit(1000.0);
        account.setAccountType(AccountType.SAVINGS);

        accountService.createAccount(account);

        try {
            emailService.sendActivationEmail(user.getEmail(), token);
            logger.info("Sent email to user: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to bro oksend activation email to user: {}", user.getEmail(), e);
        }
    }

    public boolean activateUser(String token) {
        logger.info("Activating user with token: {}", token);
        Optional<User> userOptional = userRepository.findByActivationToken(token);


        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.isEnabled()) {
                logger.warn("User {} is already activated", user.getEmail());
                return false;
            }
            user.setEnabled(true);
            user.setActivationToken(null);
            userRepository.save(user);
            logger.info("User {} activated successfully", user.getEmail());
            changeAccountStatusToActive(user);
            return true;
        } else {
            logger.warn("Invalid activation token: {}", token);
            return false;
        }
    }

    private void changeAccountStatusToActiveAllAccounts(User user) {
        List<Account> accounts = accountService.getAccountsByUserId(user.getId());

        if (accounts.isEmpty()) {
            logger.warn("No accounts found for user {}", user.getEmail());
            return;
        }

        for (Account account : accounts) {
            account.setStatus(AccountStatus.ACTIVE);
            accountRepository.save(account);
            logger.info("Account {} status updated to ACTIVE", account.getAccountNumber());
        }
    }

    private void changeAccountStatusToActive(User user) {
        Account account = accountRepository.findFirstByUserId(user.getId());

        if (account == null) {
            logger.warn("No account found for user {}", user.getEmail());
            return;
        }

        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
        logger.info("Account {} status updated to ACTIVE", account.getAccountNumber());
    }

    @Transactional
    public User updateUser(User user) {
        if (userRepository.existsById(user.getId())) {
            return userRepository.save(user);
        } else {
            throw new RuntimeException("Usuario no encontrado");
        }
    }
}
