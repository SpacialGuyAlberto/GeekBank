package com.geekbank.bank.services;

import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.models.MainScreenGiftCardItem;
import com.geekbank.bank.models.MainScreenGiftCardItemDTO;
import com.geekbank.bank.repositories.MainScreenGiftCardItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

    /**
     * Obtiene todos los elementos de tarjetas de regalo para la pantalla principal con sus detalles.
     *
     * @return Lista de MainScreenGiftCardItemDTO
     */
    public List<MainScreenGiftCardItemDTO> getMainScreenGiftCardItems() {
        List<MainScreenGiftCardItem> mainScreenGiftCardItems = mainScreenGiftCardItemRepository.findAll();
        return mainScreenGiftCardItems.stream()
                .map(item -> {
                    KinguinGiftCard giftcard = kinguinService.fetchGiftCardById(String.valueOf(item.getProductId()))
                            .orElse(null);
                    return new MainScreenGiftCardItemDTO(item, giftcard);
                })
                .collect(Collectors.toList());
    }

    /**
     * Agrega una lista de productos como elementos de tarjetas de regalo para la pantalla principal.
     *
     * @param productIds Lista de IDs de productos
     * @return Lista de MainScreenGiftCardItem agregados
     */
    public List<MainScreenGiftCardItem> addItems(List<Long> productIds) {
        List<MainScreenGiftCardItem> newItems = productIds.stream()
                .map(productId -> {
                    MainScreenGiftCardItem item = new MainScreenGiftCardItem();
                    item.setProductId(productId);
                    return mainScreenGiftCardItemRepository.save(item);
                })
                .collect(Collectors.toList());
        logger.info("Agregados {} nuevos elementos de tarjetas de regalo para la pantalla principal.", newItems.size());
        return newItems;
    }

    /**
     * Elimina los elementos de tarjetas de regalo para la pantalla principal basados en una lista de IDs de productos.
     *
     * @param productIds Lista de IDs de productos a eliminar
     */
    @Transactional
    public void removeItems(List<Long> productIds) {
        logger.debug("Intentando eliminar elementos de tarjetas de regalo con IDs de producto: {}", productIds);
        mainScreenGiftCardItemRepository.deleteByProductIdIn(productIds);
        List<MainScreenGiftCardItem> remainingItems = mainScreenGiftCardItemRepository.findByProductIdIn(productIds);
        if (remainingItems.isEmpty()) {
            logger.debug("Se eliminaron correctamente los elementos de tarjetas de regalo para los IDs de producto: {}", productIds);
        } else {
            logger.error("No se pudieron eliminar todos los elementos de tarjetas de regalo para los IDs de producto: {}", productIds);
        }
    }
}