package com.geekbank.bank.controllers;

import com.geekbank.bank.models.GifcardClassification;
import com.geekbank.bank.models.MainScreenGiftCardItem;
import com.geekbank.bank.models.MainScreenGiftCardItemDTO;
import com.geekbank.bank.repositories.MainScreenGiftCardItemRepository;
import com.geekbank.bank.services.MainScreenGiftCardService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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

    @DeleteMapping("/{productId}")
    @Transactional
    public ResponseEntity<Void> removeByProductId(@PathVariable String productId) {
        Optional<MainScreenGiftCardItem> item = mainScreenGiftCardItemRepository.findByProductId(Long.valueOf(productId));
        item.ifPresent(mainScreenGiftCardItemRepository::delete);
        return ResponseEntity.noContent().build(); // 204 OK
    }
}