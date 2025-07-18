

package com.geekbank.bank.payment.tigo.model;

import com.geekbank.bank.order.dto.OrderRequest;

public class VerifyPaymentRequest {
    private String refNumber;
    private String phoneNumber;
    private OrderRequest orderRequest;


    public String getRefNumber() {
        return refNumber;
    }

    public void setRefNumber(String refNumber) {
        this.refNumber = refNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public OrderRequest getOrderRequest() {
        return orderRequest;
    }

    public void setOrderRequest(OrderRequest orderRequest) {
        this.orderRequest = orderRequest;
    }
}
