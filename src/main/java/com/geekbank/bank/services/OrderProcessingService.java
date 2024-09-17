package com.geekbank.bank.services;

import com.geekbank.bank.models.OrderRequest;
import com.geekbank.bank.models.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OrderProcessingService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private OrderRequestStorageService orderRequestStorageService;

    public void processOrderFromTelegramMessage(String text) {
        Pattern pattern = Pattern.compile("some_regex_to_extract_phone_and_order_details");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String phoneNumber = matcher.group(1);

            OrderRequest orderRequest = orderRequestStorageService.getOrderRequestByPhoneNumber(phoneNumber);
            if (orderRequest != null) {
                OrderResponse orderResponse = orderService.placeOrder(orderRequest);

                if (orderResponse != null) {
                    // After successful order placement
                    smsService.sendPaymentNotification(phoneNumber);
                    orderRequestStorageService.removeOrderRequest(phoneNumber);
                    // Download keys or any additional post-order process
                } else {
                    System.out.println("Order failed to process");
                }
            } else {
                System.out.println("No matching Order Request found.");
            }
        } else {
            System.out.println("Message did not match expected pattern.");
        }
    }
}
