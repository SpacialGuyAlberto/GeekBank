package com.geekbank.bank.controllers;

import com.geekbank.bank.models.Customer;
import com.geekbank.bank.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/{customerId}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable long customerId) {
        Customer customer = customerService.getCustomerById(customerId);
        if (customer != null) {
            return ResponseEntity.ok(customer);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        Customer createdCustomer = customerService.createCustomer(customer);
        return ResponseEntity.created(URI.create("/api/customers/" + createdCustomer.getId())).body(createdCustomer);
    }

}

