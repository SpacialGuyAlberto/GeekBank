package com.geekbank.bank.repositories;

import com.geekbank.bank.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test") // Indica que se usará application-test.properties
public class CartItemRepositoryIntegrationTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository; // Inyectar el repositorio de usuarios

    private User user1;
    private User user2;

    @BeforeEach
    public void setup() {
        // Limpia las tablas antes de cada prueba
        cartItemRepository.deleteAll();
        userRepository.deleteAll();

        // Crear y guardar usuarios de prueba
        user1 = new User();
        user1.setEmail("testuser1@example.com");
        user1.setEnabled(true);
        user1.setActivationToken("token-001");
        user1.setName("Test User One");
        user1.setPassword("{bcrypt}$2a$10$...");
        user1.setPhoneNumber("1112223333");
        user1.setRole(Roles.CUSTOMER);

        user2 = new User();
        user2.setEmail("testuser2@example.com");
        user2.setEnabled(false);
        user2.setActivationToken("token-002");
        user2.setName("Test User Two");
        user2.setPassword("{bcrypt}$2a$10$...");
        user2.setPhoneNumber("4445556666");
        user2.setRole(Roles.ADMIN);

        userRepository.save(user1);
        userRepository.save(user2);

        // Crear y guardar CartItems de prueba
        CartItem cartItem1 = new CartItem();
        cartItem1.setUser(user1);
        cartItem1.setProductId(1001L);
        cartItem1.setQuantity(2);

        CartItem cartItem2 = new CartItem();
        cartItem2.setUser(user1);
        cartItem2.setProductId(1002L);
        cartItem2.setQuantity(1);

        CartItem cartItem3 = new CartItem();
        cartItem3.setUser(user2);
        cartItem3.setProductId(2001L);
        cartItem3.setQuantity(5);

        cartItemRepository.save(cartItem1);
        cartItemRepository.save(cartItem2);
        cartItemRepository.save(cartItem3);
    }

    @Test
    @DisplayName("Buscar CartItems por User existente")
    public void testFindByUser_ExistingUser() {
        List<CartItem> cartItems = cartItemRepository.findByUser(user1);
        assertNotNull(cartItems, "La lista de cart items no debería ser nula");
        assertEquals(2, cartItems.size(), "Debería haber dos cart items para el usuario1");
    }

    @Test
    @DisplayName("Buscar CartItem por User y ProductId existentes")
    public void testFindByUserAndProductId_ExistingUserAndProductId() {
        CartItem cartItem = cartItemRepository.findByUserAndProductId(user1, 1001L);
        assertNotNull(cartItem, "El cart item debería existir");
        assertEquals(2, cartItem.getQuantity(), "La cantidad debería ser 2");
    }

    @Test
    @DisplayName("Eliminar CartItem por ProductId")
    public void testDeleteByCustomQuery() {
        // Asegurar que existe el CartItem con productId=1002L para user1
        CartItem cartItem = cartItemRepository.findByUserAndProductId(user1, 1002L);
        assertNotNull(cartItem, "El cart item con productId=1002L debería existir");

        // Ejecutar la eliminación
        cartItemRepository.deleteByCustomQuery(1002L);

        // Verificar que ya no existe
        CartItem deletedCartItem = cartItemRepository.findByUserAndProductId(user1, 1002L);
        assertNull(deletedCartItem, "El cart item con productId=1002L debería haber sido eliminado");
    }

    @Test
    @DisplayName("Eliminar todos los CartItems por User")
    public void testDeleteAllByUser() {
        // Ejecutar la eliminación
        cartItemRepository.deleteAllByUser(user1);

        // Verificar que ya no existen CartItems para user1
        List<CartItem> cartItems = cartItemRepository.findByUser(user1);
        assertTrue(cartItems.isEmpty(), "No debería haber cart items para el usuario1");

        // Asegurar que los CartItems de user2 siguen existiendo
        List<CartItem> user2CartItems = cartItemRepository.findByUser(user2);
        assertEquals(1, user2CartItems.size(), "Debería haber una cart item para el usuario2");
    }

    @Test
    @DisplayName("Actualizar cantidad de CartItem por ProductId y User")
    public void testUpdateQuantityByProductIdAndUser() {
        // Ejecutar la actualización
        cartItemRepository.updateQuantityByProductIdAndUser(1001L, 5, user1);

        // Verificar la actualización
        CartItem updatedCartItem = cartItemRepository.findByUserAndProductId(user1, 1001L);
        assertNotNull(updatedCartItem, "El cart item debería existir");
        assertEquals(5, updatedCartItem.getQuantity(), "La cantidad debería haber sido actualizada a 5");
    }

    @Test
    @DisplayName("Buscar CartItems por User y ProductId inexistentes")
    public void testFindByUserAndProductId_NonExisting() {
        CartItem cartItem = cartItemRepository.findByUserAndProductId(user1, 9999L);
        assertNull(cartItem, "No debería existir un cart item con el productId proporcionado para el usuario1");
    }
}
