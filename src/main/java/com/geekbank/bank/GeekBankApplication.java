package com.geekbank.bank;

import com.geekbank.bank.services.TelegramListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GeekBankApplication implements CommandLineRunner {

    @Autowired
    private TelegramListener telegramListener;

    public static void main(String[] args) {
        SpringApplication.run(GeekBankApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("Starting Telegram Listener in a new thread...");
        Thread telegramThread = new Thread(() -> {
            try {
                telegramListener.listenForMessages();
            } catch (Exception e) {
                System.out.println("Exception in telegramThread: " + e.getMessage());
                e.printStackTrace();
            }
        });
        telegramThread.setDaemon(true); // Optional: Make this a daemon thread
        telegramThread.start();
        System.out.println("Telegram Listener started.");
    }
}