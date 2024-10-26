package com.geekbank.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.models.HighlightItem;
import com.geekbank.bank.models.HighlightItemWithGiftcardDTO;
import com.geekbank.bank.services.HighlightService;
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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HighlightController.class)
@ActiveProfiles("test")
public class HighlightControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HighlightService highlightService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/highlights - Obtener elementos destacados")
    public void testGetHighlights() throws Exception {
        List<HighlightItemWithGiftcardDTO> highlights = List.of(new HighlightItemWithGiftcardDTO());
        when(highlightService.getHighlightsByProductIds()).thenReturn(highlights);

        mockMvc.perform(get("/api/highlights")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(highlights.size()));

        verify(highlightService, times(1)).getHighlightsByProductIds();
    }

    @Test
    @DisplayName("POST /api/highlights - Agregar elementos destacados")
    public void testAddHighlights() throws Exception {
        HighlightController.HighlightRequest request = new HighlightController.HighlightRequest();
        request.setProductIds(List.of(1L, 2L, 3L));

        List<HighlightItem> addedHighlights = List.of(new HighlightItem(), new HighlightItem());
        when(highlightService.addHighlightItems(request.getProductIds())).thenReturn(addedHighlights);

        mockMvc.perform(post("/api/highlights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(addedHighlights.size()));

        verify(highlightService, times(1)).addHighlightItems(request.getProductIds());
    }

    @Test
    @DisplayName("DELETE /api/highlights - Eliminar elementos destacados")
    public void testRemoveHighlights() throws Exception {
        List<Long> productIds = List.of(1L, 2L, 3L);

        mockMvc.perform(delete("/api/highlights")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productIds)))
                .andExpect(status().isNoContent());

        verify(highlightService, times(1)).removeHighlightItems(productIds);
    }
}
