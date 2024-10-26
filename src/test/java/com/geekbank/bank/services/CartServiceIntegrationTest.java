package com.geekbank.bank.services;

import com.geekbank.bank.models.CartItem;
import com.geekbank.bank.models.User;
import com.geekbank.bank.models.KinguinGiftCard;
import com.geekbank.bank.models.CartItemWithGiftcardDTO;
import com.geekbank.bank.repositories.CartItemRepository;
import com.geekbank.bank.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    public void setUp() {
        cartItemRepository.deleteAll();
        userRepository.deleteAll();

        // Crear un usuario para usar en las pruebas
        testUser = new User();
        testUser.setEmail("testuser@example.com");
        testUser.setName("Test User");
        testUser.setPassword("{bcrypt}$2a$10$...");
        userRepository.save(testUser);
    }

    @Test
    @DisplayName("Agregar un artículo al carrito y verificar que se guarda correctamente")
    public void testAddCartItem() {
        Long productId = 1L;
        int quantity = 2;

        CartItem addedItem = cartService.addCartItem(testUser, productId, quantity);

        assertNotNull(addedItem.getId(), "El artículo del carrito debería tener un ID después de guardarse");
        assertEquals(testUser.getId(), addedItem.getUser().getId(), "El artículo debería estar asociado con el usuario correcto");
        assertEquals(productId, addedItem.getProductId(), "El ID del producto debería coincidir");
        assertEquals(quantity, addedItem.getQuantity(), "La cantidad del artículo debería coincidir con la proporcionada");
    }

    @Test
    @DisplayName("Obtener artículos del carrito para un usuario")
    public void testGetCartItems() {
        // Agregar un artículo al carrito
        cartService.addCartItem(testUser, 1L, 2);

        List<CartItemWithGiftcardDTO> cartItems = cartService.getCartItems(testUser);

        assertFalse(cartItems.isEmpty(), "El carrito debería contener artículos");
        assertEquals(1, cartItems.size(), "El carrito debería contener exactamente un artículo");
        assertEquals(testUser.getEmail(), cartItems.get(0).getCartItem().getUser().getEmail(), "El artículo debería estar asociado con el usuario correcto");
    }

    @Test
    @DisplayName("Actualizar la cantidad de un artículo en el carrito")
    public void testUpdateCartItemQuantity() {
        Long productId = 1L;
        int initialQuantity = 2;
        int updatedQuantity = 5;

        cartService.addCartItem(testUser, productId, initialQuantity);
        cartService.updateCartItemQuantity(productId, updatedQuantity, testUser);

        CartItem updatedItem = cartItemRepository.findByUserAndProductId(testUser, productId);
        assertEquals(updatedQuantity, updatedItem.getQuantity(), "La cantidad del artículo debería actualizarse correctamente");
    }

    @Test
    @DisplayName("Eliminar todos los artículos del carrito de un usuario")
    public void testRemoveAllCartItems() {
        // Agregar varios artículos al carrito
        cartService.addCartItem(testUser, 1L, 2);
        cartService.addCartItem(testUser, 2L, 3);

        cartService.removeAllCartItems(testUser);

        List<CartItem> remainingItems = cartItemRepository.findByUser(testUser);
        assertTrue(remainingItems.isEmpty(), "Todos los artículos deberían eliminarse del carrito");
    }

    @Test
    @DisplayName("Eliminar un artículo específico del carrito")
    public void testRemoveCartItem() {
        // Agregar un artículo al carrito
        CartItem cartItem = cartService.addCartItem(testUser, 1L, 2);

        cartService.removeCartItem(cartItem.getId());

        assertFalse(cartItemRepository.existsById(cartItem.getId()), "El artículo debería eliminarse del carrito");
    }
}
