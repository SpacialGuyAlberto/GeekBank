package com.geekbank.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.models.User;
import com.geekbank.bank.models.WishedItem;
import com.geekbank.bank.models.WishedItemGiftcardDTO;
import com.geekbank.bank.services.UserService;
import com.geekbank.bank.services.WishService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class WishListControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WishService wishService;

    @MockBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/wish-list/{wishedItemId} - Obtener artículo de la lista de deseos por ID (Usuario autenticado)")
    public void testGetWishedItemById_AuthenticatedUser() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test.user@example.com");

        User user = new User();
        user.setId(1L);
        user.setEmail("test.user@example.com");

        WishedItemGiftcardDTO wishedItem = new WishedItemGiftcardDTO();
        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(wishService.getWishedItem(any(Long.class))).thenReturn(wishedItem);

        mockMvc.perform(get("/api/wish-list/{wishedItemId}", 1L)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(wishService, times(1)).getWishedItem(1L);
    }

    @Test
    @DisplayName("POST /api/wish-list - Agregar un artículo a la lista de deseos")
    public void testAddWishedItem() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test.user@example.com");

        User user = new User();
        user.setId(1L);
        user.setEmail("test.user@example.com");

        // Crear JSON de la solicitud simulando los datos enviados desde el frontend
        String addWishedItemRequestJson = objectMapper.writeValueAsString(
                new WishListController.AddWishedItemRequest() {{
                    setProductId(1L);
                    setQuantity(2);
                }

                    private void setQuantity(int i) {
                    }
                }
        );

        WishedItem wishedItem = new WishedItem();
        wishedItem.setId(1L);

        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(wishService.addWishedItem(any(User.class), eq(1L), eq(2))).thenReturn(wishedItem);

        mockMvc.perform(post("/api/wish-list")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addWishedItemRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(wishedItem.getId()));

        verify(wishService, times(1)).addWishedItem(user, 1L, 2);
    }

    private void setProductId(long l) {
    }

    @Test
    @DisplayName("DELETE /api/wish-list/{wishedItemId} - Eliminar un artículo de la lista de deseos")
    public void testRemoveWishedItem() throws Exception {
        Long wishedItemId = 1L;

        mockMvc.perform(delete("/api/wish-list/{wishedItemId}", wishedItemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(wishService, times(1)).removeWishedItem(wishedItemId);
    }

    @Test
    @DisplayName("DELETE /api/wish-list - Eliminar todos los artículos de la lista de deseos del usuario autenticado")
    public void testRemoveAllWishedItems() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test.user@example.com");

        User user = new User();
        user.setId(1L);
        user.setEmail("test.user@example.com");

        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/wish-list")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(wishService, times(1)).removeAllWishedItems(user);
    }
}
