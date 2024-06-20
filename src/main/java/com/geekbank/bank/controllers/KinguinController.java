package com.geekbank.bank.controllers;

import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.services.KinguinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kinguin")
public class KinguinController {

    @Autowired
    private KinguinService kinguinService;

    @GetMapping("/gift-cards")
    public List<KinguinGiftCard> getGiftCards(@RequestParam(value = "page", defaultValue = "1") int page) {
        return kinguinService.fetchGiftCards(page);
    }

    @GetMapping("/gift-cards/{id}")
    @CrossOrigin(origins = "http://localhost:4200")
    public KinguinGiftCard getGiftCardById(@PathVariable String id) {
        return kinguinService.fetchGiftCardById(id);
    }
}
