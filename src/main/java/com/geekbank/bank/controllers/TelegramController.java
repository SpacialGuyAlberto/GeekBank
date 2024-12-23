// src/main/java/com/geekbank/bank/controllers/TelegramController.java
package com.geekbank.bank.controllers;

import com.geekbank.bank.services.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/telegram")
public class TelegramController {

    private final SmsService smsService;

    @Autowired
    public TelegramController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/update")
    public void handleUpdate(@RequestBody UpdateRequest request) {
        String message = request.getMessage();
        System.out.println("Received message: " + message);

        // Process the message and extract phone number and amount
        // Assuming you have the logic to process the message here
        // Send SMS
        String phoneNumber = null;
        smsService.sendPaymentNotification(null);
    }

    public static class UpdateRequest {
        private String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
