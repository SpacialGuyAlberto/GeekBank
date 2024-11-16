package com.geekbank.bank.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class SmsService {
    public static final String ACCOUNT_SID = "AC98e3cdfd26052f6078bf60c1f7c5b281";
    public static final String AUTH_TOKEN = "e8c079aae192453898a185f280f95be9";

    public SmsService() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendPaymentNotification(String toPhoneNumber) {
        Message message = Message.creator(
                        new PhoneNumber(toPhoneNumber),
                        new PhoneNumber("+13343043656"),
                        "dein code: §$Q2" )
                .create();

        System.out.println("Message sent: " + message.getSid());
    }

    public void sendKeysToPhoneNumber(String phoneNumber, List<String> key) {
        Message message = Message.creator(
                        new PhoneNumber(phoneNumber),
                        new PhoneNumber("+13343043656"),
                        "tu codigo es: " + Arrays.toString(key.toArray()))
                .create();

        System.out.println("Message sent: " + message.getSid() + "to" + phoneNumber);
    }
}
