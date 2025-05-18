package com.geekbank.bank.config;

import com.geekbank.bank.support.sync.service.GiftCardSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    @Autowired
    private GiftCardSyncService giftCardSyncService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduleGiftCardSync() {
        giftCardSyncService.syncGiftCards();
    }
}
