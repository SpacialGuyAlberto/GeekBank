package com.geekbank.bank.controllers;

import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.models.OrderResponse;
import com.geekbank.bank.services.TigoService;
import jakarta.persistence.Entity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.geekbank.bank.services.OrderService;
import com.geekbank.bank.services.SmsService;
import com.geekbank.bank.models.OrderRequest;
import com.geekbank.bank.services.OrderRequestStorageService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private TigoService tigoService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRequestStorageService orderRequestStorageService;

    @Autowired
    private SmsService smsService;



    @PostMapping
    public void placeOrder(@RequestBody OrderRequest orderRequest) {
        orderRequestStorageService.storeOrderRequest(orderRequest); // Almacena la orden en espera
    }

    @PostMapping("/order-updated")
    public void handleOrderUpdated(@RequestHeader("X-Event-Name") String eventName,
                                   @RequestBody Map<String, Object> payload) {
        if ("order.status".equals(eventName)) {
            String orderId = (String) payload.get("orderId");
            String status = (String) payload.get("status");

            if ("completed".equals(status)) {
                // Descarga las keys de la orden
                List<String> keys = orderService.downloadKeys(orderId);
                // Envía las keys al número de teléfono del cliente
                String phoneNumber = orderService.getPhoneNumberByOrderId(orderId);
                smsService.sendKeysToPhoneNumber(phoneNumber, keys);
            }
        }
    }




//    public static class OrderRequest {
//        private String phoneNumber;
//        private List<KinguinGiftCard> products;
//
//        // Getters y Setters
//
//        public String getPhoneNumber() {
//            return phoneNumber;
//        }
//
//        public void setPhoneNumber(String phoneNumber) {
//            this.phoneNumber = phoneNumber;
//        }
//
//        public List<KinguinGiftCard> getProducts() {
//            return products;
//        }
//
//        public void setProducts(List<KinguinGiftCard> products) {
//            this.products = products;
//        }
//    }
}
