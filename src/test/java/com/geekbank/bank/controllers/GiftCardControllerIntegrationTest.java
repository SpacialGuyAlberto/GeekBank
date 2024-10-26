package com.geekbank.bank.controllers;

import com.geekbank.bank.models.GeneralGiftCard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GiftCardController.class)
@ActiveProfiles("test")
public class GiftCardControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /api/gift-cards/free-fire - Obtener detalles de la tarjeta Free Fire")
    public void testGetFreeFireGiftCard() throws Exception {
        mockMvc.perform(get("/api/gift-cards/free-fire")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("1234-5678-9012"))
                .andExpect(jsonPath("$.name").value("Free Fire Diamonds"))
                .andExpect(jsonPath("$.price").value(1.9))
                .andExpect(jsonPath("$.expiryDate").value("2025-12-31"));
    }

    @Test
    @DisplayName("GET /api/gift-cards/call-of-duty - Obtener detalles de la tarjeta Call of Duty")
    public void testGetCallOfDutyGiftCard() throws Exception {
        mockMvc.perform(get("/api/gift-cards/call-of-duty")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("1234-5678-9012"))
                .andExpect(jsonPath("$.name").value("Call of Duty Battle Pass"))
                .andExpect(jsonPath("$.price").value(43.99))
                .andExpect(jsonPath("$.expiryDate").value("2025-12-31"));
    }

    @Test
    @DisplayName("GET /api/gift-cards/fortnite - Obtener detalles de la tarjeta Fortnite")
    public void testGetFortniteGiftCard() throws Exception {
        mockMvc.perform(get("/api/gift-cards/fortnite")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("1234-5678-9012"))
                .andExpect(jsonPath("$.name").value("Fornite Battle Pass"))
                .andExpect(jsonPath("$.price").value(43.99))
                .andExpect(jsonPath("$.expiryDate").value("2025-12-31"));
    }

    @Test
    @DisplayName("GET /api/gift-cards/fifa - Obtener detalles de la tarjeta FIFA")
    public void testGetFifaGiftCard() throws Exception {
        mockMvc.perform(get("/api/gift-cards/fifa")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("1234-5678-9012"))
                .andExpect(jsonPath("$.name").value("Fifa 24"))
                .andExpect(jsonPath("$.price").value(43.99))
                .andExpect(jsonPath("$.expiryDate").value("2025-12-31"));
    }
}
