package com.geekbank.bank.config;

import com.paypal.sdk.Environment;
import com.paypal.sdk.PaypalServerSdkClient;
import com.paypal.sdk.authentication.ClientCredentialsAuthModel;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayPalConfiguration {

    @Value("${PAYPAL_CLIENT_ID}")
    private String paypalClientId;

    @Value("${PAYPAL_CLIENT_SECRET}")
    private String paypalClientSecret;

    // Si necesitas usar baseUrl en otra parte, puedes inyectarlo aquí también
    @Value("${PAYPAL_BASE_URL}")
    private String paypalBaseUrl;

    @Bean
    public PaypalServerSdkClient paypalClient() {
        return new PaypalServerSdkClient.Builder()
                .loggingConfig(builder -> builder
                        .level(Level.DEBUG)
                        .requestConfig(logConfigBuilder -> logConfigBuilder.body(true))
                        .responseConfig(logConfigBuilder -> logConfigBuilder.headers(true)))
                .httpClientConfig(configBuilder -> configBuilder.timeout(0))
                // SANDBOX o LIVE según tu entorno
                .environment(Environment.PRODUCTION)
                // Por defecto: clientCredentialsAuth
                .clientCredentialsAuth(
                        new ClientCredentialsAuthModel.Builder(paypalClientId, paypalClientSecret).build()
                )
                .build();
    }
}
