package com.geekbank.bank.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.geekbank.bank.models.CartItem;
import com.geekbank.bank.models.CartItemWithGiftcardDTO;
import com.geekbank.bank.models.User;
import com.geekbank.bank.services.CartService;
import com.geekbank.bank.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@ActiveProfiles("test")
public class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("GET /api/cart - Obtener los artículos del carrito (Usuario autenticado)")
    public void testGetCartItems_AuthenticatedUser() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test.user@example.com");

        User user = new User();
        user.setId(1L);
        user.setEmail("test.user@example.com");

        List<CartItemWithGiftcardDTO> cartItems = List.of(new CartItemWithGiftcardDTO());
        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartService.getCartItems(user)).thenReturn(cartItems);

        mockMvc.perform(get("/api/cart")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cartService, times(1)).getCartItems(user);
    }

    @Test
    @DisplayName("POST /api/cart - Agregar un artículo al carrito")
    public void testAddCartItem() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test.user@example.com");

        User user = new User();
        user.setId(1L);
        user.setEmail("test.user@example.com");

        // Crear el JSON simulando los datos de la solicitud del frontend
        String addCartItemRequestJson = objectMapper.writeValueAsString(
                Map.of(
                        "productId", 1L,
                        "quantity", 2,
                        "price", 19.99
                )
        );

        CartItem cartItem = new CartItem();
        cartItem.setId(1L);

        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cartService.addCartItem(user, 1L, 2,45.6)).thenReturn(cartItem);

        mockMvc.perform(post("/api/cart")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addCartItemRequestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cartItem.getId()));

        verify(cartService, times(1)).addCartItem(user, 1L, 2, 45.6);
    }

    @Test
    @DisplayName("PUT /api/cart - Actualizar la cantidad de un artículo del carrito")
    public void testUpdateCartItemQuantity() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test.user@example.com");

        User user = new User();
        user.setId(1L);
        user.setEmail("test.user@example.com");

        // Crear el JSON de la solicitud que simula los datos de actualización del frontend
        String updateRequestJson = objectMapper.writeValueAsString(
                Map.of(
                        "productId", 1L,
                        "quantity", 5
                )
        );

        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        mockMvc.perform(put("/api/cart")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestJson))
                .andExpect(status().isOk());

        verify(cartService, times(1)).updateCartItemQuantity(1L, 5, user);
    }

    @Test
    @DisplayName("DELETE /api/cart/{cartItemId} - Eliminar un artículo del carrito")
    public void testRemoveCartItem() throws Exception {
        Long cartItemId = 1L;

        mockMvc.perform(delete("/api/cart/{cartItemId}", cartItemId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cartService, times(1)).removeCartItem(cartItemId);
    }

    @Test
    @DisplayName("DELETE /api/cart - Eliminar todos los artículos del carrito del usuario autenticado")
    public void testRemoveAllCartItems() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("test.user@example.com");

        User user = new User();
        user.setId(1L);
        user.setEmail("test.user@example.com");

        when(userService.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        mockMvc.perform(delete("/api/cart")
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(cartService, times(1)).removeAllCartItems(user);
    }
}
