package com.geekbank.bank.combo.service;

import com.geekbank.bank.combo.model.ComboEntity;
import com.geekbank.bank.combo.repository.ComboRepository;
import com.geekbank.bank.giftcard.model.GiftCardEntity;
import com.geekbank.bank.giftcard.repository.GiftCardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComboService {

    private final ComboRepository comboRepository;
    private final GiftCardRepository giftCardRepository;

    public ComboService(ComboRepository comboRepository, GiftCardRepository giftCardRepository) {
        this.comboRepository = comboRepository;
        this.giftCardRepository = giftCardRepository;
    }

    public List<ComboEntity> getAllActiveCombos() {
        return comboRepository.findByIsActiveTrue();
    }

    public ComboEntity getComboById(Long id) {
        return comboRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Combo not found with id: " + id));
    }

    @Transactional
    public ComboEntity createCombo(ComboEntity combo, List<Long> productIds) {
        List<GiftCardEntity> products = giftCardRepository.findAllById(productIds);
        if (products.isEmpty()) {
            throw new RuntimeException("No valid products found for the combo");
        }

        combo.setProducts(products);
        validateProfitMargin(combo);

        return comboRepository.save(combo);
    }

    @Transactional
    public ComboEntity updateCombo(Long id, ComboEntity updatedCombo, List<Long> productIds) {
        ComboEntity existingCombo = getComboById(id);

        if (productIds != null && !productIds.isEmpty()) {
            List<GiftCardEntity> products = giftCardRepository.findAllById(productIds);
            existingCombo.setProducts(products);
        }

        existingCombo.setName(updatedCombo.getName());
        existingCombo.setDescription(updatedCombo.getDescription());
        existingCombo.setPrice(updatedCombo.getPrice());
        existingCombo.setImageUrl(updatedCombo.getImageUrl());
        existingCombo.setIsActive(updatedCombo.getIsActive());

        validateProfitMargin(existingCombo);

        return comboRepository.save(existingCombo);
    }

    public void deleteCombo(Long id) {
        comboRepository.deleteById(id);
    }

    private void validateProfitMargin(ComboEntity combo) {
        double totalCost = combo.getProducts().stream()
                .mapToDouble(product -> product.getCost() != null ? product.getCost() : 0.0)
                .sum();

        if (combo.getPrice() <= totalCost) {
            throw new RuntimeException("The combo price (" + combo.getPrice()
                    + ") must be greater than the total cost of products (" + totalCost + ") to ensure profit.");
        }

        // Calculate and set transient profit margin for display/logic
        combo.setProfitMargin(combo.getPrice() - totalCost);
    }
}
