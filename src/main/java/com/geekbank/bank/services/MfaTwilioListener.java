package com.geekbank.bank.services;

import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.PostConstruct;

@Service
public class MfaTwilioListener {

    // Definir las credenciales de Twilio directamente en la clase
    public static final String ACCOUNT_SID = "AC0b4bcd81cb31a12b9898f38f99fe046c";
    public static final String AUTH_TOKEN = "88a9682bc5133433d5ae970032f66ca8";

    @Autowired
    private final SmsService smsService;

    public MfaTwilioListener(SmsService smsService) {
        this.smsService = smsService;
    }

    @PostConstruct
    public void startListener(){
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
        Thread listenerThread = new Thread(this::listenForMfaMessages);
        listenerThread.setDaemon(true);  // Daemon thread para que no bloquee la finalización de la aplicación
        listenerThread.start();
    }

    public void listenForMfaMessages() {
        while (true) {
            try {
                // Aquí implementarías la lógica para escuchar y procesar mensajes de Twilio
                // Esto podría involucrar obtener mensajes recientes de Twilio, y procesarlos como MFA

                // Ejemplo: Buscar mensajes con el texto de verificación
                // Nota: Twilio no tiene una API directa para escuchar SMS entrantes en tiempo real
                // pero puedes usar una función Webhook para recibir notificaciones de nuevos mensajes.

                // Simulación de recibir un mensaje
                String incomingMessageBody = "Tu código de verificación es 123456"; // Simulado
                String incomingPhoneNumber = "+123456789"; // Simulado

                // Procesar el mensaje recibido
                processReceivedMessage(incomingMessageBody, incomingPhoneNumber);

            } catch (Exception e) {
                e.printStackTrace();
            }

            // Sleep to avoid hitting API rate limits
            try {
                Thread.sleep(10000); // Revisa cada 10 segundos (ajusta según sea necesario)
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void processReceivedMessage(String body, String fromPhoneNumber) {
        // Procesa el código MFA recibido y realiza las acciones necesarias
        System.out.println("Received MFA Code from " + fromPhoneNumber + ": " + body);

        // Aquí podrías almacenar el código MFA para usarlo más tarde o validarlo
        // Ejemplo: Enviar una notificación SMS confirmando la recepción del código
        smsService.sendPaymentNotification(fromPhoneNumber);
    }
}
