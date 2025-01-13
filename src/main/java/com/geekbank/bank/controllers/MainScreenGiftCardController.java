package com.geekbank.bank.controllers;

import com.geekbank.bank.models.MainScreenGiftCardItem;
import com.geekbank.bank.models.MainScreenGiftCardItemDTO;
import com.geekbank.bank.services.MainScreenGiftCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/main-screen-gift-cards")
public class MainScreenGiftCardController {

    private final MainScreenGiftCardService mainScreenGiftCardService;

    @Autowired
    public MainScreenGiftCardController(MainScreenGiftCardService mainScreenGiftCardService) {
        this.mainScreenGiftCardService = mainScreenGiftCardService;
    }

    @GetMapping
    public ResponseEntity<Page<MainScreenGiftCardItemDTO>> getMainScreenGiftCardItems(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MainScreenGiftCardItemDTO> items = mainScreenGiftCardService.getMainScreenGiftCardItems(pageable);
        return ResponseEntity.ok(items);
    }

    @PostMapping
    public ResponseEntity<List<MainScreenGiftCardItem>> addMainScreenGiftCardItems(@RequestBody MainScreenGiftCardRequest request) {
        List<MainScreenGiftCardItem> addedItems = mainScreenGiftCardService.addItems(request.getProductIds());
        return ResponseEntity.ok(addedItems);
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