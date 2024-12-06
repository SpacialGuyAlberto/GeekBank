package com.geekbank.bank.services;

import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.models.User;
import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class SendGridEmailService {

    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${DOMAIN_ORIGIN_URL}")
    private String domainUrl;

    public void sendActivationEmail(String to, String token) {
        Email from = new Email("lalbertomurillo1996@gmail.com"); // Cambia esto a tu email
        Email toEmail = new Email(to);
        String subject = "Email de activación";
        String activationLink = domainUrl + "/activate?token=" + token;
        String body = "Haga clic en el siguiente enlace para activar su cuenta: " + activationLink;
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("Response Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            System.out.println("Response Headers: " + response.getHeaders());
        } catch (IOException ex) {
            System.err.println("Failed to send email to: " + to);
            ex.printStackTrace();
        }
    }

    public void sendNotificationEmail(String to) {
        Email from = new Email("lalbertomurillo1996@gmail.com"); // Cambia esto a tu email
        Email toEmail = new Email(to);
        String subject = "Nueva transaccion manual solicitada";
        String body = "Se ha solicitado una nueva transaccion manual. Por favor complete la transaccion.";
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("Response Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            System.out.println("Response Headers: " + response.getHeaders());
        } catch (IOException ex) {
            System.err.println("Failed to send email to: " + to);
            ex.printStackTrace();
        }
    }

    public void sendPurchaseConfirmationEmail(String to, List<String> key, Transaction transaction) {
        Email from = new Email("lalbertomurillo1996@gmail.com"); // Cambia esto a tu email
        Email toEmail = new Email(to);
        String subject = "Confirmacion de compra";
        String body = "Compra exitosa de tus keys: " + key + "\n Tu numero de transaccion es: " + transaction.getTransactionNumber();
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("Response Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            System.out.println("Response Headers: " + response.getHeaders());
        } catch (IOException ex) {
            System.err.println("Failed to send email to: " + to);
            ex.printStackTrace();
        }
    }


    public void sendSetPasswordEmail(User user) {
        // Configurar las direcciones de email
        Email from = new Email("lalbertomurillo1996@gmail.com");
        Email toEmail = new Email(user.getEmail());

        // Asunto del email
        String subject = "Establece tu contraseña en [Nombre de tu Aplicación]"; // Reemplaza con el nombre de tu aplicación

        // Construir el enlace de activación
        String activationLink = domainUrl + "/set-password?token=" + user.getActivationToken();

        // Contenido del email en HTML
        String body = "<p>Hola " + user.getName() + ",</p>"
                + "<p>Tu cuenta ha sido creada. Por favor, haz clic en el siguiente enlace para establecer tu contraseña y activar tu cuenta:</p>"
                + "<p><a href=\"" + activationLink + "\">Establecer contraseña</a></p>"
                + "<p>Si no solicitaste esta cuenta, por favor ignora este correo.</p>";

        // Configurar el contenido del email
        Content content = new Content("text/html", body);

        // Crear el objeto Mail
        Mail mail = new Mail(from, subject, toEmail, content);

        // Inicializar SendGrid
        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            System.out.println("Código de respuesta: " + response.getStatusCode());
            System.out.println("Cuerpo de la respuesta: " + response.getBody());
            System.out.println("Encabezados de la respuesta: " + response.getHeaders());
        } catch (IOException ex) {
            System.err.println("Error al enviar email a: " + user.getEmail());
            ex.printStackTrace();
        }
    }
}
