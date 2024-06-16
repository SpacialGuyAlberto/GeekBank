package com.geekbank.bank.services;

import com.geekbank.bank.models.CallOfDutyGiftCard;
import org.springframework.stereotype.Service;

@Service
public class CallOfDutyGiftCardService {
    public CallOfDutyGiftCard getCallOfDutyGiftCard() {
        return new CallOfDutyGiftCard("5678-1234-9012", "Call of Duty Modern Warfare Points", 49.99, "2025-12-31");
    }
}