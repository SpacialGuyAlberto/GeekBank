package com.geekbank.bank.services;

import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.models.MainScreenGiftCardItem;
import com.geekbank.bank.models.MainScreenGiftCardItemDTO;
import com.geekbank.bank.repositories.MainScreenGiftCardItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MainScreenGiftCardService {

    private final MainScreenGiftCardItemRepository mainScreenGiftCardItemRepository;
    private final KinguinService kinguinService;

    private static final Logger logger = LoggerFactory.getLogger(MainScreenGiftCardService.class);

    @Autowired
    public MainScreenGiftCardService(MainScreenGiftCardItemRepository mainScreenGiftCardItemRepository, KinguinService kinguinService) {
        this.mainScreenGiftCardItemRepository = mainScreenGiftCardItemRepository;
        this.kinguinService = kinguinService;
    }

    public Page<MainScreenGiftCardItemDTO> getMainScreenGiftCardItems(Pageable pageable) {
        Page<MainScreenGiftCardItem> pageOfItems = mainScreenGiftCardItemRepository.findAllOrdered(pageable);

        return pageOfItems.map(item -> {
            KinguinGiftCard giftcard = kinguinService.fetchGiftCardById(String.valueOf(item.getProductId()))
                    .orElse(null);
            return new MainScreenGiftCardItemDTO(item, giftcard);
        });
    }


    public List<MainScreenGiftCardItem> addItems(List<Long> productIds) {
        // 1. Ver cuáles ya existen en la BD
        List<Long> existingProductIds = mainScreenGiftCardItemRepository
                .findByProductIdIn(productIds)     // Retorna los registros existentes
                .stream()
                .map(MainScreenGiftCardItem::getProductId)
                .collect(Collectors.toList());

        // 2. Filtrar sólo los IDs nuevos, ignorando duplicados
        List<Long> newProductIds = productIds.stream()
                .distinct() // quita duplicados dentro de la misma lista
                .filter(id -> !existingProductIds.contains(id))
                .collect(Collectors.toList());

        // 3. Crear entidades para los nuevos IDs y guardarlas
        List<MainScreenGiftCardItem> newItems = newProductIds.stream()
                .map(productId -> {
                    MainScreenGiftCardItem item = new MainScreenGiftCardItem();
                    item.setProductId(productId);
                    return mainScreenGiftCardItemRepository.save(item);
                })
                .collect(Collectors.toList());

        logger.info("Agregados {} nuevos elementos de tarjetas de regalo (sin repetir).", newItems.size());
        return newItems;
    }


    @Transactional
    public void removeItems(List<Long> productIds) {
        logger.debug("Intentando eliminar elementos de tarjetas de regalo con IDs de producto: {}", productIds);

        // Llamamos directamente al método del repositorio que borra solo los registros con esos IDs.
        mainScreenGiftCardItemRepository.deleteByProductIdIn(productIds);

        // Verificamos si efectivamente se eliminaron
        List<MainScreenGiftCardItem> remainingItems = mainScreenGiftCardItemRepository.findByProductIdIn(productIds);
        if (remainingItems.isEmpty()) {
            logger.debug("Se eliminaron correctamente los elementos de tarjetas de regalo para los IDs de producto: {}", productIds);
        } else {
            logger.error("No se pudieron eliminar todos los elementos de tarjetas de regalo para los IDs de producto: {}", productIds);
        }
    }

}