package com.geekbank.bank;

import com.geekbank.bank.config.ConfigLoader;
import com.geekbank.bank.services.TelegramListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.autoconfigure.domain.EntityScan;


@SpringBootApplication
@EnableScheduling
@EntityScan(basePackages = {"com.geekbank.bank.models"})
@EnableJpaRepositories(basePackages = "com.geekbank.bank.repositories")
public class GeekBankApplication  {

    @Autowired
    private TelegramListener telegramListener;

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure()
                .directory("./")
                .load();

        System.setProperty("SPRING_APPLICATION_NAME", dotenv.get("SPRING_APPLICATION_NAME"));
        System.setProperty("SPRING_DATASOURCE_URL", dotenv.get("SPRING_DATASOURCE_URL"));
        System.setProperty("SPRING_DATASOURCE_USERNAME", dotenv.get("SPRING_DATASOURCE_USERNAME"));
        System.setProperty("SPRING_DATASOURCE_PASSWORD", dotenv.get("SPRING_DATASOURCE_PASSWORD"));
        System.setProperty("SERVER_PORT", dotenv.get("SERVER_PORT"));
        System.setProperty("JWT_SECRET", dotenv.get("JWT_SECRET"));
        System.setProperty("SPRING_MAIL_HOST", dotenv.get("SPRING_MAIL_HOST"));
        System.setProperty("SPRING_MAIL_PORT", dotenv.get("SPRING_MAIL_PORT"));
        System.setProperty("SPRING_MAIL_USERNAME", dotenv.get("SPRING_MAIL_USERNAME"));
        System.setProperty("SPRING_MAIL_PASSWORD", dotenv.get("SPRING_MAIL_PASSWORD"));
        System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
        System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
        System.setProperty("SPRING_SENDGRID_API_KEY", dotenv.get("SPRING_SENDGRID_API_KEY"));
        System.setProperty("DOMAIN_URL", dotenv.get("DOMAIN_URL"));
        System.setProperty("DOMAIN_ORIGIN_URL", dotenv.get("DOMAIN_ORIGIN_URL"));
        System.setProperty("PAYPAL_CLIENT_ID", dotenv.get("PAYPAL_CLIENT_ID"));
        System.setProperty("PAYPAL_CLIENT_SECRET", dotenv.get("PAYPAL_CLIENT_SECRET"));
        System.setProperty("PAYPAL_BASE_URL", dotenv.get("PAYPAL_BASE_URL"));
        System.setProperty("STRIPE_API_KEY", dotenv.get("STRIPE_API_KEY"));

        SpringApplication.run(GeekBankApplication.class, args);
    }
}