package com.geekbank.bank.order.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.geekbank.bank.order.dto.OrderRequest;

@Service
public class OrderRequestStorageService {

    private ConcurrentHashMap<String, OrderRequest> pendingOrders = new ConcurrentHashMap<>();

    public void storeOrderRequest(OrderRequest orderRequest) {
        pendingOrders.put(orderRequest.getPhoneNumber(), orderRequest);
        System.out.println("Stored OrderRequest for phone number: " + orderRequest.getPhoneNumber());
    }

    public OrderRequest getOrderRequestByPhoneNumber(String phoneNumber) {
        OrderRequest orderRequest = pendingOrders.get(phoneNumber);
        System.out.println("Retrieved OrderRequest for phone number: " + phoneNumber + " -> " + (orderRequest != null ? "Found" : "Not Found"));
        return orderRequest;
    }

    public void removeOrderRequest(String phoneNumber) {
        System.out.println("Attempting to remove OrderRequest for phone number: [" + phoneNumber + "]");

        // Iterar manualmente y eliminar la orden
        boolean removed = false;
        for (Map.Entry<String, OrderRequest> entry : pendingOrders.entrySet()) {
            if (entry.getKey().equals(phoneNumber.trim())) {
                pendingOrders.remove(entry.getKey());
                removed = true;
                break;
            }
        }

        System.out.println("Removal of OrderRequest " + (removed ? "succeeded" : "failed") + " for phone number: [" + phoneNumber + "]");
    }

    public void removeOrderRequestById(String orderRequestId){
        System.out.println("Attempting to remove order request Id: " +  orderRequestId);

        boolean removed = false;

        for (Map.Entry<String, OrderRequest> entry : pendingOrders.entrySet()){
            OrderRequest orderRequest = entry.getValue();

            if (orderRequest.getOrderRequestId().equals(orderRequestId)){
                pendingOrders.remove(entry.getKey());
                removed = true;
                break;
            }
        }

        System.out.println("Removal of Order request by ID: " + (removed ? "succeeded" : "failed for ID: " + orderRequestId));
    }

    @Scheduled(fixedRate = 60000)
    public void expireOrderRequests() {
        LocalDateTime now = LocalDateTime.now();
        long expirationMinutes = 5;

        for (Map.Entry<String, OrderRequest> entry : pendingOrders.entrySet()) {
            OrderRequest orderRequest = entry.getValue();
            if (orderRequest.getCreatedAt().plusMinutes(expirationMinutes).isBefore(now)) {
                pendingOrders.remove(entry.getKey());
                System.out.println("OrderRequest expired and removed for phone number: " + entry.getKey());
            }
        }
    }


    public boolean hasOrderForPhoneNumber(String phoneNumber) {
        boolean hasOrder = pendingOrders.containsKey(phoneNumber);
        System.out.println("Checking if OrderRequest exists for phone number: " + phoneNumber + " -> " + (hasOrder ? "Exists" : "Does Not Exist"));
        return hasOrder;
    }
}
