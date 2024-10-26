package com.geekbank.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.services.RecommendationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
@ActiveProfiles("test")
public class RecommendationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RecommendationService recommendationService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/recommendations/user/{userId} - Obtener recomendaciones basadas en el usuario")
    public void testGetRecommendationsByUser() throws Exception {
        Long userId = 1L;
        List<KinguinGiftCard> mockRecommendations = List.of(
                new KinguinGiftCard("1234-5678-9012", "Fortnite V-Bucks", 19.99, "2025-12-31"),
                new KinguinGiftCard("8765-4321-0987", "Minecraft Coins", 9.99, "2025-12-31")
        );

        when(recommendationService.recommend(userId, 4)).thenReturn(mockRecommendations);

        mockMvc.perform(get("/api/recommendations/user/{userId}", userId)
                        .param("k", "4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(mockRecommendations.size()))
                .andExpect(jsonPath("$[0].productName").value("Fortnite V-Bucks"))
                .andExpect(jsonPath("$[1].productName").value("Minecraft Coins"));
    }

    @Test
    @DisplayName("GET /api/recommendations/popular - Obtener recomendaciones populares")
    public void testGetMostPopularRecommendations() throws Exception {
        List<KinguinGiftCard> mockPopularRecommendations = List.of(
                new KinguinGiftCard("9999-8888-7777", "Roblox Robux", 15.99, "2025-12-31"),
                new KinguinGiftCard("1111-2222-3333", "League of Legends RP", 25.00, "2025-12-31")
        );

        when(recommendationService.recommendByPopularity(4)).thenReturn(mockPopularRecommendations);

        mockMvc.perform(get("/api/recommendations/popular")
                        .param("k", "4")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(mockPopularRecommendations.size()))
                .andExpect(jsonPath("$[0].productName").value("Roblox Robux"))
                .andExpect(jsonPath("$[1].productName").value("League of Legends RP"));
    }
}
