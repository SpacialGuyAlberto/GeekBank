//package com.geekbank.bank.services;
//
//import com.twilio.rest.api.v2010.account.Message;
//import com.twilio.type.PhoneNumber;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.MockedStatic;
//import org.mockito.Mockito;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//@ActiveProfiles("test")
//public class SmsServiceIntegrationTest {
//
//    @InjectMocks
//    private SmsService smsService;
//
//    private MockedStatic<Message> messageMock;
//
//    @BeforeEach
//    public void setup() {
//        // Mockeo estático de la clase Message para evitar llamadas reales a Twilio
//        messageMock = Mockito.mockStatic(Message.class);
//    }
//
//    @Test
//    @DisplayName("Enviar notificación de pago")
//    public void testSendPaymentNotification() {
//        // Configuración de mocks
//        Message mockMessage = mock(Message.class);
//        when(mockMessage.getSid()).thenReturn("MockSID12345");
//
//        // Simular la creación de un mensaje
//        Message.Creator creatorMock = mock(Message.Creator.class);
//        when(creatorMock.create()).thenReturn(mockMessage);
//
//        // Configuración para devolver el creador del mensaje cuando se llame al método creator
//        messageMock.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString()))
//                .thenReturn(creatorMock);
//
//        String toPhoneNumber = "+1234567890";
//        smsService.sendPaymentNotification(toPhoneNumber);
//
//        verify(creatorMock, times(1)).create();
//        assertEquals("MockSID12345", mockMessage.getSid(), "El SID del mensaje debería ser MockSID12345");
//    }
//
//    @Test
//    @DisplayName("Enviar claves al número de teléfono")
//    public void testSendKeysToPhoneNumber() {
//        Message mockMessage = mock(Message.class);
//        when(mockMessage.getSid()).thenReturn("MockSID67890");
//
//        Message.Creator creatorMock = mock(Message.Creator.class);
//        when(creatorMock.create()).thenReturn(mockMessage);
//
//        messageMock.when(() -> Message.creator(any(PhoneNumber.class), any(PhoneNumber.class), anyString()))
//                .thenReturn(creatorMock);
//
//        String phoneNumber = "+1234567890";
//        List<String> keys = List.of("Key1", "Key2", "Key3");
//
//        smsService.sendKeysToPhoneNumber(phoneNumber, keys);
//
//        verify(creatorMock, times(1)).create();
//        assertEquals("MockSID67890", mockMessage.getSid(), "El SID del mensaje debería ser MockSID67890");
//    }
//}
