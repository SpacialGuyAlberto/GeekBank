package com.geekbank.bank.support.sync.service;

import com.geekbank.bank.giftcard.kinguin.service.KinguinService;
import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;
import com.geekbank.bank.giftcard.model.GiftCardEntity;
import com.geekbank.bank.giftcard.repository.GiftCardRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/* ──────────────────────────────────────────────────────────────
 * 1) Servicio que INSERTA una GiftCard en una transacción propia
 * ────────────────────────────────────────────────────────────── */
@Service
class GiftCardPersistenceService {

    private final GiftCardRepository giftCardRepository;

    GiftCardPersistenceService(GiftCardRepository giftCardRepository) {
        this.giftCardRepository = giftCardRepository;
    }

    /** Cada llamada abre una transacción nueva: si falla NO afecta a las demás. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(GiftCardEntity entity) {
        giftCardRepository.save(entity);
        // Opcional: giftCardRepository.flush();
    }
}

/* ──────────────────────────────────────────────────────────────
 * 2) Servicio principal de sincronización
 * ────────────────────────────────────────────────────────────── */
@Service
public class GiftCardSyncService {

    private final KinguinService kinguinService;
    private final GiftCardRepository giftCardRepository;
    private final GiftCardPersistenceService persistenceService;

    private static final Logger log = LoggerFactory.getLogger(GiftCardSyncService.class);

    /* -- Estado para mostrar avance desde el frontend (opcional) -- */
    private static final AtomicInteger progress = new AtomicInteger(0);
    private static final AtomicBoolean isSyncing = new AtomicBoolean(false);
    private static final AtomicBoolean cancelSync = new AtomicBoolean(false);
    private static volatile int totalGiftCards = 0;
    private static volatile int synchronizedGiftCards = 0;

    public GiftCardSyncService(KinguinService kinguinService,
                               GiftCardRepository giftCardRepository,
                               GiftCardPersistenceService persistenceService) {
        this.kinguinService = kinguinService;
        this.giftCardRepository = giftCardRepository;
        this.persistenceService = persistenceService;
    }

    /* ---------- getters para UI ---------- */
    public static int  getProgress()               { return progress.get(); }
    public static int  getTotalGiftCards()         { return totalGiftCards; }
    public static int  getSynchronizedGiftCards()  { return synchronizedGiftCards; }
    public static boolean isSyncing()              { return isSyncing.get(); }
    public void cancelSync()                       { cancelSync.set(true); }

    /* ---------- Sincronización asíncrona ---------- */
    @Async
    public void syncGiftCards() {

        /* Impide lanzarla dos veces a la vez */
        if (!isSyncing.compareAndSet(false, true)) {
            log.warn("Sincronización ya en progreso.");
            return;
        }

        try {
            resetCounters();

            List<KinguinGiftCard> all = kinguinService.fetchAllGiftCards();
            totalGiftCards = all.size();
            log.info("GiftCards recibidas de API: {}", totalGiftCards);

            for (KinguinGiftCard kCard : all) {

                if (cancelSync.get()) {
                    log.info("Sincronización cancelada por el usuario.");
                    break;
                }

                long id = kCard.getKinguinId();
                if (giftCardRepository.existsById(id)) {
                    advanceProgress();
                    continue;
                }

                GiftCardEntity entity = map(kCard);

                try {
                    /* ► Inserción aislada en REQUIRES_NEW */
                    persistenceService.save(entity);
                    log.debug("GiftCard {} guardada OK.", id);
                } catch (Exception ex) {
                    /* Solo se revierte esta tarjeta; la sincronización continúa */
                    log.error("Error al guardar GiftCard {}: {}", id, ex.getMessage(), ex);
                }

                advanceProgress();
            }

            log.info("Fin de sincronización. Total sincronizadas correctamente: {}", synchronizedGiftCards);

        } catch (Exception ex) {
            log.error("Error global de sincronización: {}", ex.getMessage(), ex);

        } finally {
            isSyncing.set(false);
            progress.set(100);
        }
    }

    /* ---------- Utilidades internas ---------- */
    private void resetCounters() {
        progress.set(0);
        synchronizedGiftCards = 0;
        totalGiftCards = 0;
        cancelSync.set(false);
    }

    private void advanceProgress() {
        synchronizedGiftCards++;
        progress.set((int) ((synchronizedGiftCards / (double) totalGiftCards) * 100));
    }

    private GiftCardEntity map(KinguinGiftCard k) {
        GiftCardEntity e = new GiftCardEntity();
        e.setKinguinId((long) k.getKinguinId());
        e.setProductId(        cut(k.getProductId(),            255));
        e.setName(             cut(k.getProduct(),              255));
        e.setDescription(      cut(k.getDescription(),         1000));
        e.setPrice(                 k.getPrice());
        e.setReleaseDate(          k.getExpirationDate());
        e.setPlatform(         cut(k.getPlatform(),             100));
        e.setQty(                   k.getQty());
        e.setTextQty(               k.getTextQty());
        e.setRegionalLimitations(cut(k.getRegionalLimitations(),500));
        e.setRegionId(              k.getRegionId());
        e.setActivationDetails( cut(k.getActivationDetails(),  1000));
        e.setOriginalName(      cut(k.getOriginalName(),        500));
        e.setOffersCount(           k.getOffersCount());
        e.setTotalQty(              k.getTotalQty());
        e.setAgeRating(         cut(k.getAgeRating(),           100));
        return e;
    }

    private String cut(String v, int max) {
        return v != null && v.length() > max ? v.substring(0, max) : v;
    }
}
