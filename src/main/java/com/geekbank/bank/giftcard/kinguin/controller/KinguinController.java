package com.geekbank.bank.giftcard.kinguin.controller;

import com.deepl.api.DeepLException;
import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;
import com.geekbank.bank.giftcard.kinguin.service.KinguinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
        return  kinguinService.fetchGiftCardById(id);
    }

    @GetMapping("/gift-cards-details/{id}")
    @CrossOrigin(origins = "http://localhost:4200")
    public KinguinGiftCard getGiftCardDetailsById(@PathVariable String id) throws DeepLException, InterruptedException {
        KinguinGiftCard giftCard =  kinguinService.fetchGiftCardById(id);
        giftCard.setDescription(kinguinService.translateText(giftCard.getDescription()));
        giftCard.setActivationDetails(kinguinService.translateText(giftCard.getActivationDetails()));
        return giftCard;
    }

    @GetMapping("/gift-cards/search")
    public List<KinguinGiftCard> searchGiftCards(@RequestParam(value = "name") String name) {
        return kinguinService.searchGiftCardsByName(name);
    }

    @GetMapping("/gift-cards/filter")
    public List<KinguinGiftCard> getFilteredGiftCards(@RequestParam Map<String, String> filters) {
        return kinguinService.fetchFilteredGiftCards(filters);
    }
}
