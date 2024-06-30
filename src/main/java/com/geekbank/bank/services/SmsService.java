package com.geekbank.bank.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.stereotype.Service;

@Service
public class SmsService {
    // Encuentra tu SID de cuenta y el token en https://www.twilio.com/console
    public static final String ACCOUNT_SID = "AC0b4bcd81cb31a12b9898f38f99fe046c";
    public static final String AUTH_TOKEN = "88a9682bc5133433d5ae970032f66ca8";

    public SmsService() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public void sendPaymentNotification(String toPhoneNumber) {
        Message message = Message.creator(
                        new PhoneNumber(toPhoneNumber),
                        new PhoneNumber("+18134135566"),
                        "guten taaaag" )
                .create();

        System.out.println("Message sent: " + message.getSid());
    }

}
