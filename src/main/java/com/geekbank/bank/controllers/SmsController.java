package com.geekbank.bank.controllers;

import com.geekbank.bank.services.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    private final SmsService smsService;

    @Autowired
    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostMapping("/send")
    public String sendSms(@RequestParam String toPhoneNumber) {
        smsService.sendPaymentNotification(toPhoneNumber);
        return "SMS sent to " + toPhoneNumber;
    }
}
