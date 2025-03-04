package com.geekbank.bank.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "unmatched_payments")
public class UnmatchedPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String phoneNumber;
    private double amountReceived;
    private String referenceNumber;
    private LocalDateTime receivedAt;

    @Column(name="consumed", nullable = false, columnDefinition = "boolean default false")
    private boolean consumed = false;

    @Column(name = "differenceredeemed", nullable = false, columnDefinition = "boolean default false")
    private boolean differenceRedeemed = false;


    @Column(name= "verified", nullable = false, columnDefinition = "boolean default false")
    private boolean verified = false;

    @OneToOne
    @JoinColumn(name = "sms_message_id")
    private SmsMessage smsMessage;

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Lob
    @Column(name = "image", nullable = true)
    private byte[] image;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    private String imagePath;

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public UnmatchedPayment() {
    }

    public UnmatchedPayment(String phoneNumber, double amountReceived, String referenceNumber, LocalDateTime receivedAt, SmsMessage smsMessage) {
        this.phoneNumber = phoneNumber;
        this.amountReceived = amountReceived;
        this.referenceNumber = referenceNumber;
        this.receivedAt = receivedAt;
        this.smsMessage = smsMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public double getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(double amountReceived) {
        this.amountReceived = amountReceived;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public SmsMessage getSmsMessage() {
        return smsMessage;
    }

    public void setSmsMessage(SmsMessage smsMessage) {
        this.smsMessage = smsMessage;
    }
    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }
    public boolean isDifferenceRedeemed() {
        return differenceRedeemed;
    }

    public void setDifferenceRedeemed(boolean differenceRedeemed) {
        this.differenceRedeemed = differenceRedeemed;
    }
    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }
}
