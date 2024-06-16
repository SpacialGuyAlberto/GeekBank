package com.geekbank.bank.controllers;

import com.geekbank.bank.models.FreeFireGiftCard;
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
}
