package com.geekbank.bank.config;

import com.geekbank.bank.services.GiftCardSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Autowired
    private GiftCardSyncService giftCardSyncService;

    // Ejecutar la sincronización una vez al día a las 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduleGiftCardSync() {
        giftCardSyncService.syncGiftCards();
    }
}
