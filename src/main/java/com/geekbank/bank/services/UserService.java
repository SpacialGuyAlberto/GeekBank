package com.geekbank.bank.services;

import com.geekbank.bank.models.User;
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
    private final SendGridEmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, SendGridEmailService emailService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
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
            return true;
        } else {
            logger.warn("Invalid activation token: {}", token);
            return false;
        }
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
