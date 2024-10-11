package com.geekbank.bank.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Autowired
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendActivationEmail(String to, String token) {
        String subject = "Email de activación";
        String body = "Haga clic en el siguiente enlace para activar su cuenta: http://localhost:7070/activate?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);
            System.out.println("Sent email to user: " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email to: " + to);
            e.printStackTrace();
        }
    }


}
