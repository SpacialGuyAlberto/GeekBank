package com.geekbank.bank.services;

import com.geekbank.bank.models.GiftCardEntity;
import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.repositories.GiftCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class GiftCardSyncService {

    @Autowired
    private KinguinService giftCardService;

    @Autowired
    private GiftCardRepository giftCardRepository;

    private static final Logger logger = LoggerFactory.getLogger(GiftCardSyncService.class);

    private static AtomicInteger progress = new AtomicInteger(0);
    private static boolean isSyncing = false;
    private static int totalGiftCards = 0;
    private static int synchronizedGiftCards = 0;

    private static AtomicBoolean cancelSync = new AtomicBoolean(false);

    public static int getProgress() {
        return progress.get();
    }

    public static boolean isSyncing() {
        return isSyncing;
    }

    public static int getTotalGiftCards() {
        return totalGiftCards;
    }

    public static int getSynchronizedGiftCards() {
        return synchronizedGiftCards;
    }

    public void cancelSync() {
        cancelSync.set(true);
        logger.info("Sincronización cancelada por el usuario.");
    }

    @Transactional
    @Async
    public void syncGiftCards() {
        if (isSyncing) {
            logger.warn("Sincronización ya en progreso.");
            return;
        }

        isSyncing = true;
        progress.set(0);
        totalGiftCards = 0;
        synchronizedGiftCards = 0;
        cancelSync.set(false);
        logger.info("Iniciando sincronización de GiftCards desde la API externa.");

        try {
            List<KinguinGiftCard> allGiftCards = giftCardService.fetchAllGiftCards();
            totalGiftCards = allGiftCards.size();
            logger.info("Total de GiftCards obtenidas de la API: {}", totalGiftCards);

            for (KinguinGiftCard kCard : allGiftCards) {
                if (cancelSync.get()) {
                    logger.info("Sincronización interrumpida. Finalizando proceso.");
                    throw new RuntimeException("Sincronización cancelada por el usuario.");
                }

                if (!giftCardRepository.existsById((long) kCard.getKinguinId())) {
                    GiftCardEntity entity = new GiftCardEntity();
                    entity.setKinguinId((long) kCard.getKinguinId());
                    entity.setProductId(truncate(kCard.getProductId(), 255));
                    entity.setName(truncate(kCard.getProduct(), 255));
                    entity.setDescription(truncate(kCard.getDescription(), 1000));
                    entity.setPrice(kCard.getPrice());
                    entity.setReleaseDate(kCard.getExpirationDate());
                    entity.setPlatform(truncate(kCard.getPlatform(), 100));
                    entity.setQty(kCard.getQty());
                    entity.setTextQty(kCard.getTextQty());
                    entity.setRegionalLimitations(truncate(kCard.getRegionalLimitations(), 500));
                    entity.setRegionId(kCard.getRegionId());
                    entity.setActivationDetails(truncate(kCard.getActivationDetails(), 1000));
                    entity.setOriginalName(truncate(kCard.getOriginalName(), 500));
                    entity.setOffersCount(kCard.getOffersCount());
                    entity.setTotalQty(kCard.getTotalQty());
                    entity.setAgeRating(truncate(kCard.getAgeRating(), 100));

                    logger.debug("Insertando GiftCard ID {} con los siguientes datos:", entity.getKinguinId());
                    logger.debug("Product ID: {}", entity.getProductId());
                    logger.debug("Name: {}", entity.getName());
                    logger.debug("Description: {}", entity.getDescription());
                    logger.debug("Activation Details: {}", entity.getActivationDetails());
                    logger.debug("Regional Limitations: {}", entity.getRegionalLimitations());
                    logger.debug("Age Rating: {}", entity.getAgeRating());

                    giftCardRepository.save(entity);
                    giftCardRepository.flush();

                    synchronizedGiftCards++;
                    progress.set((int) ((synchronizedGiftCards / (double) totalGiftCards) * 100));

                    logger.info("GiftCard ID {} insertada exitosamente.", entity.getKinguinId());
                } else {
                    logger.debug("GiftCard ID {} ya existe en la base de datos. Omitiendo inserción.", kCard.getKinguinId());
                    synchronizedGiftCards++;
                    progress.set((int) ((synchronizedGiftCards / (double) totalGiftCards) * 100));
                }
            }

            logger.info("Sincronización de GiftCards completada. Total de GiftCards sincronizadas: {}", synchronizedGiftCards);
        } catch (Exception e) {
            logger.error("Error durante la sincronización de GiftCards: {}", e.getMessage(), e);
            throw e;
        } finally {
            isSyncing = false;
            progress.set(100);
        }
    }



    private String truncate(String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            return value.substring(0, maxLength);
        }
        return value;
    }

    @Async
    public void insertTestGiftCard() {
        List<KinguinGiftCard> allGiftCards = giftCardService.fetchAllGiftCards();
        totalGiftCards = allGiftCards.size();
        logger.info("Total de GiftCards obtenidas de la API: {}", totalGiftCards);

        GiftCardEntity entity = new GiftCardEntity();
        entity.setKinguinId(99999L);
        entity.setProductId("test_product_id");
        entity.setName("Test GiftCard");
        entity.setDescription("Descripción de prueba.");
        entity.setPrice(100.0);
        entity.setReleaseDate("2025-12-31");
        entity.setPlatform("PC");
        entity.setQty(10);
        entity.setTextQty(1);
        entity.setRegionalLimitations("REGION FREE");
        entity.setRegionId(1);
        entity.setActivationDetails("Detalles de activación.");
        entity.setOriginalName("GiftCard Original");
        entity.setOffersCount(5);
        entity.setTotalQty(50);
        entity.setAgeRating("PEGI 18");

        giftCardRepository.save(entity);
        logger.info("GiftCard de prueba insertada con ID: {}", entity.getKinguinId());
    }
}
