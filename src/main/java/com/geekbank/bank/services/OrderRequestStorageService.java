package com.geekbank.bank.services;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.geekbank.bank.models.OrderRequest;

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


    public boolean hasOrderForPhoneNumber(String phoneNumber) {
        boolean hasOrder = pendingOrders.containsKey(phoneNumber);
        System.out.println("Checking if OrderRequest exists for phone number: " + phoneNumber + " -> " + (hasOrder ? "Exists" : "Does Not Exist"));
        return hasOrder;
    }
}
