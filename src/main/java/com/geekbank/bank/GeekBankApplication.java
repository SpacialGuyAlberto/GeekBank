package com.geekbank.bank;

//import com.geekbank.bank.services.TelegramListener;
import com.geekbank.bank.services.TelegramListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.geekbank.bank.repositories")
public class GeekBankApplication implements CommandLineRunner{

    @Autowired
    private TelegramListener telegramListener;

    public static void main(String[] args) {
        SpringApplication.run(GeekBankApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        telegramListener.listenForMessages();
    }
}
