package com.geekbank.bank.controllers;

import com.geekbank.bank.models.GifcardClassification;
import com.geekbank.bank.models.MainScreenGiftCardItem;
import com.geekbank.bank.models.MainScreenGiftCardItemDTO;
import com.geekbank.bank.repositories.MainScreenGiftCardItemRepository;
import com.geekbank.bank.services.MainScreenGiftCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/main-screen-gift-cards")
public class MainScreenGiftCardController {

    private final MainScreenGiftCardService mainScreenGiftCardService;
    private final MainScreenGiftCardItemRepository mainScreenGiftCardItemRepository;

    @Autowired
    public MainScreenGiftCardController(MainScreenGiftCardService mainScreenGiftCardService, MainScreenGiftCardItemRepository mainScreenGiftCardItemRepository) {
        this.mainScreenGiftCardService = mainScreenGiftCardService;
        this.mainScreenGiftCardItemRepository = mainScreenGiftCardItemRepository;
    }

    @GetMapping
    public ResponseEntity<Page<MainScreenGiftCardItemDTO>> getMainScreenGiftCardItems(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        System.out.println("GET /main-screen-gift-cards?page=" + page + "&size=" + size);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
        Page<MainScreenGiftCardItemDTO> items = mainScreenGiftCardService.getMainScreenGiftCardItems(pageable);

        System.out.println("Retornando " + items.getContent().size() + " elementos. totalPages=" + items.getTotalPages());
        return ResponseEntity.ok(items);
    }


    @GetMapping(params = "classification")
    public ResponseEntity<List<MainScreenGiftCardItemDTO>> getGiftCardsByClassification(
            @RequestParam GifcardClassification classification) {

        List<MainScreenGiftCardItemDTO> items = mainScreenGiftCardService.getMainScreenGiftCardItemsByClassification(classification);
        return ResponseEntity.ok(items);
    }



    @PostMapping
    public ResponseEntity<MainScreenGiftCardItem> add(@RequestBody MainScreenGiftCardItemDTO item) {
        if (item == null) {
            return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok(mainScreenGiftCardService.addItem(item.getMainScreenGiftCardItem()));
    }


    @DeleteMapping
    public ResponseEntity<Void> removeMainScreenGiftCardItems(@RequestBody List<Long> productIds) {
        mainScreenGiftCardService.removeItems(productIds);
        return ResponseEntity.noContent().build();
    }

    public static class MainScreenGiftCardRequest {
        private List<Long> productIds;

        public List<Long> getProductIds() {
            return productIds;
        }

        public void setProductIds(List<Long> productIds) {
            this.productIds = productIds;
        }
    }
}