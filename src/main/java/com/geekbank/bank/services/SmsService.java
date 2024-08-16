package com.geekbank.bank.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmsService {
//    public static final String ACCOUNT_SID = "AC0b4bcd81cb31a12b9898f38f99fe046c";
    public static final String ACCOUNT_SID = "ACc9373db954ea8570303588b66a5c7a1f";
//    public static final String AUTH_TOKEN = "88a9682bc5133433d5ae970032f66ca8";
    public static final String AUTH_TOKEN = "eac67a03c162967bb67181b732580bc9";

    public SmsService() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendPaymentNotification(String toPhoneNumber) {
        Message message = Message.creator(
                        new PhoneNumber(toPhoneNumber),
                        new PhoneNumber("+18134135566"),
                        "tu codigo es: §$Q2" )
                .create();

        System.out.println("Message sent: " + message.getSid());
    }

    public void sendKeysToPhoneNumber(String phoneNumber, List<String> keys) {
        // Lógica para enviar los mensajes SMS
        String message = "Your game keys: " + String.join(", ", keys);
        // Aquí implementarías el código para enviar el SMS utilizando alguna API de SMS
        System.out.println("Sending SMS to " + phoneNumber + ": " + message);
    }
}
