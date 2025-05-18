package com.geekbank.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.support.feedback.model.Feedback;
import com.geekbank.bank.support.feedback.dto.FeedbackRequest;
import com.geekbank.bank.support.feedback.controller.FeedbackController;
import com.geekbank.bank.support.feedback.service.FeedbackService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeedbackController.class)
@ActiveProfiles("test")
public class FeedbackControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FeedbackService feedbackService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/feedbacks - Obtener todos los feedbacks")
    public void testGetAllFeedbacks() throws Exception {
        List<Feedback> feedbacks = List.of(new Feedback(), new Feedback());
        when(feedbackService.getAllFeedbacks()).thenReturn(feedbacks);

        mockMvc.perform(get("/api/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(feedbackService, times(1)).getAllFeedbacks();
    }

    @Test
    @DisplayName("GET /api/feedbacks/{id} - Obtener feedback por ID")
    public void testGetFeedbackById() throws Exception {
        Feedback feedback = new Feedback();
        feedback.setId(1L);
        when(feedbackService.getFeedbackById(1L)).thenReturn(Optional.of(feedback));

        mockMvc.perform(get("/api/feedbacks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));

        verify(feedbackService, times(1)).getFeedbackById(1L);
    }

    @Test
    @DisplayName("POST /api/feedbacks - Crear un nuevo feedback")
    public void testCreateFeedback() throws Exception {
        FeedbackRequest feedbackRequest = new FeedbackRequest();
        feedbackRequest.setMessage("Test feedback message");
        feedbackRequest.setRating(4);

        Feedback feedback = new Feedback();
        feedback.setId(1L);
        feedback.setMessage("Test feedback message");
        feedback.setRating(4);

        when(feedbackService.createFeedback(any(FeedbackRequest.class))).thenReturn(feedback);

        mockMvc.perform(post("/api/feedbacks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.message").value("Test feedback message"));

        verify(feedbackService, times(1)).createFeedback(any(FeedbackRequest.class));
    }

    @Test
    @DisplayName("GET /api/feedbacks/giftcard/{giftCardId} - Obtener feedbacks por giftCardId")
    public void testGetFeedbacksByGiftCardId() throws Exception {
        List<Feedback> feedbacks = List.of(new Feedback(), new Feedback());
        when(feedbackService.getFeedbacksByGiftCardId(1L)).thenReturn(feedbacks);

        mockMvc.perform(get("/api/feedbacks/giftcard/{giftCardId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(feedbackService, times(1)).getFeedbacksByGiftCardId(1L);
    }

    @Test
    @DisplayName("GET /api/feedbacks/user/{userId} - Obtener feedbacks por userId")
    public void testGetFeedbacksByUserId() throws Exception {
        List<Feedback> feedbacks = List.of(new Feedback(), new Feedback());
        when(feedbackService.getFeedbacksByUserId(1L)).thenReturn(feedbacks);

        mockMvc.perform(get("/api/feedbacks/user/{userId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(feedbackService, times(1)).getFeedbacksByUserId(1L);
    }

    @Test
    @DisplayName("DELETE /api/feedbacks/{id} - Eliminar feedback por ID")
    public void testDeleteFeedback() throws Exception {
        mockMvc.perform(delete("/api/feedbacks/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(feedbackService, times(1)).deleteFeedback(1L);
    }

    @Test
    @DisplayName("GET /api/feedbacks/after/{date} - Obtener feedbacks después de una fecha")
    public void testGetFeedbacksAfterDate() throws Exception {
        List<Feedback> feedbacks = List.of(new Feedback(), new Feedback());
        long dateInMillis = new Date().getTime();
        when(feedbackService.getFeedbacksAfterDate(any(Date.class))).thenReturn(feedbacks);

        mockMvc.perform(get("/api/feedbacks/after/{date}", dateInMillis)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(feedbackService, times(1)).getFeedbacksAfterDate(any(Date.class));
    }

    @Test
    @DisplayName("GET /api/feedbacks/before/{date} - Obtener feedbacks antes de una fecha")
    public void testGetFeedbacksBeforeDate() throws Exception {
        List<Feedback> feedbacks = List.of(new Feedback(), new Feedback());
        long dateInMillis = new Date().getTime();
        when(feedbackService.getFeedbacksBeforeDate(any(Date.class))).thenReturn(feedbacks);

        mockMvc.perform(get("/api/feedbacks/before/{date}", dateInMillis)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(feedbackService, times(1)).getFeedbacksBeforeDate(any(Date.class));
    }

    @Test
    @DisplayName("GET /api/feedbacks/range - Obtener feedbacks en un rango de fechas")
    public void testGetFeedbacksInDateRange() throws Exception {
        List<Feedback> feedbacks = List.of(new Feedback(), new Feedback());
        long startDate = new Date().getTime() - 1000000;
        long endDate = new Date().getTime();

        when(feedbackService.getFeedbacksInDateRange(any(Date.class), any(Date.class))).thenReturn(feedbacks);

        mockMvc.perform(get("/api/feedbacks/range")
                        .param("startDate", String.valueOf(startDate))
                        .param("endDate", String.valueOf(endDate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(feedbackService, times(1)).getFeedbacksInDateRange(any(Date.class), any(Date.class));
    }

    @Test
    @DisplayName("GET /api/feedbacks/custom-range - Obtener feedbacks en un rango de fechas personalizado")
    public void testGetFeedbacksInDateRangeCustom() throws Exception {
        List<Feedback> feedbacks = List.of(new Feedback(), new Feedback());
        long startDate = new Date().getTime() - 1000000;
        long endDate = new Date().getTime();

        when(feedbackService.getFeedbacksInDateRangeCustom(any(Date.class), any(Date.class))).thenReturn(feedbacks);

        mockMvc.perform(get("/api/feedbacks/custom-range")
                        .param("startDate", String.valueOf(startDate))
                        .param("endDate", String.valueOf(endDate))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(feedbackService, times(1)).getFeedbacksInDateRangeCustom(any(Date.class), any(Date.class));
    }
}
