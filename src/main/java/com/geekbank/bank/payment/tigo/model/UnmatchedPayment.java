package com.geekbank.bank.payment.tigo.model;

import com.geekbank.bank.support.sms.model.SmsMessage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "unmatched_payments")
@Getter
@Setter
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
}
