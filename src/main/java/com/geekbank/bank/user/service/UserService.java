package com.geekbank.bank.user.service;

import com.geekbank.bank.payment.tigo.constants.VerificationStatus;
import com.geekbank.bank.user.account.constants.AccountStatus;
import com.geekbank.bank.user.account.constants.AccountType;
import com.geekbank.bank.user.account.model.Account;
import com.geekbank.bank.user.account.repository.AccountRepository;
import com.geekbank.bank.user.model.User;
import com.geekbank.bank.user.repository.UserRepository;
import com.geekbank.bank.user.account.service.AccountService;
import com.geekbank.bank.support.email.service.SendGridEmailService;
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
        return userRepository.findAllWithAccount();
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }


    public Optional<User> getUserById(long userId) {
        return userRepository.findByIdWithAccount(userId);
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

    public void registerUserByAdmin(User user) {

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
//            emailService.sendActivationEmail(user.getEmail(), token);
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

    public Optional<User> findByActivationToken(String token) {
        return userRepository.findByActivationToken(token);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }


}
