package com.geekbank.bank.controllers;

import com.geekbank.bank.models.FreeFireGiftCard;
import com.geekbank.bank.models.CallOfDutyGiftCard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gift-cards")
public class GiftCardController {

    @GetMapping("/free-fire")
    public FreeFireGiftCard getFreeFireGiftCard() {
        return new FreeFireGiftCard("1234-5678-9012", "Free Fire Diamonds", 1.9, "2025-12-31");
    }
    @GetMapping("/call-of-duty")
    public CallOfDutyGiftCard getCallOfDutyGiftCard() {
        return new CallOfDutyGiftCard("1234-5678-9012", "Call of Duty battle pass", 43.99, "2025-12-31");
    }

}
