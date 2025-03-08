package com.geekbank.bank.models;

public class TransactionVerificationRequest {
    private Long tempPin;
    private String refNumber;
    private String phoneNumber;

    public Long getTempPin() {
        return tempPin;
    }

    public void setTempPin(Long tempPin) {
        this.tempPin = tempPin;
    }

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
}
