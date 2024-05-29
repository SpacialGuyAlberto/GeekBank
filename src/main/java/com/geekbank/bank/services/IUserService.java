package com.geekbank.bank.services;

import com.geekbank.bank.controllers.RegistrationRequest;
import com.geekbank.bank.models.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    List<User> getUsers();
    User registerUser(RegistrationRequest request);
    Optional<User> findByEmail(String email);
}
