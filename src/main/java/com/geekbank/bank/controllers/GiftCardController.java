package com.geekbank.bank.controllers;

import com.geekbank.bank.models.GeneralGiftCard;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gift-cards")
public class GiftCardController {

    @GetMapping("/free-fire")
    public GeneralGiftCard getFreeFireGiftCard() {
        return new GeneralGiftCard("1234-5678-9012", "Free Fire Diamonds", 1.9, "2025-12-31");
    }
    @GetMapping("/call-of-duty")
    public GeneralGiftCard getCallOfDutyGiftCard() {
        return new GeneralGiftCard("1234-5678-9012", "Call of Duty Battle Pass", 43.99, "2025-12-31");
    }

    @GetMapping("/fortnite")
    public GeneralGiftCard getFortniteGiftCard() {
        return new GeneralGiftCard("1234-5678-9012", "Fornite Battle Pass", 43.99, "2025-12-31");
    }

    @GetMapping("/fifa")
    public GeneralGiftCard getFifaGiftCard() {
        return new GeneralGiftCard("1234-5678-9012", "Fifa 24", 43.99, "2025-12-31");
    }


}
