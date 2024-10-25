package com.geekbank.bank.repositories;

import com.geekbank.bank.models.WishedItem;
import com.geekbank.bank.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class WishedItemRepositoryIntegrationTest {

    @Autowired
    private WishedItemRepository wishedItemRepository;

    @Autowired
    private UserRepository userRepository;

    private WishedItem wishedItem1;
    private WishedItem wishedItem2;
    private User user;

    @BeforeEach
    public void setup() {
        wishedItemRepository.deleteAll();
        userRepository.deleteAll();

        // Crear un usuario de prueba
        user = new User();
        user.setEmail("testuser@example.com");
        user.setName("Test User");
        user.setPassword("{bcrypt}$2a$10$...");
        user.setEnabled(true);
        user = userRepository.save(user);

        // Crear y guardar ítems deseados (WishedItems) de prueba
        wishedItem1 = new WishedItem();
        wishedItem1.setUser(user);
        wishedItem1.setProductId(101L);
        wishedItem1.setQuantity(2);

        wishedItem2 = new WishedItem();
        wishedItem2.setUser(user);
        wishedItem2.setProductId(102L);
        wishedItem2.setQuantity(3);

        wishedItemRepository.save(wishedItem1);
        wishedItemRepository.save(wishedItem2);
    }

    @Test
    @DisplayName("Buscar ítems deseados por usuario")
    public void testFindByUser() {
        List<WishedItem> wishedItems = wishedItemRepository.findByUser(user);
        assertEquals(2, wishedItems.size(), "Debería encontrar dos ítems deseados para el usuario dado");
    }

    @Test
    @DisplayName("Buscar ítem deseado por ID de producto")
    public void testFindByProductId() {
        WishedItem foundItem = wishedItemRepository.findByProductId(101L);
        assertNotNull(foundItem, "Debería encontrar un ítem deseado para el ID de producto dado");
        assertEquals(101L, foundItem.getProductId(), "El ID de producto debería coincidir con 101");
    }

    @Test
    @DisplayName("Buscar ítem deseado por usuario e ID de producto")
    public void testFindByUserAndProductId() {
        WishedItem foundItem = wishedItemRepository.findByUserAndProductId(user, 102L);
        assertNotNull(foundItem, "Debería encontrar un ítem deseado para el usuario y el ID de producto dado");
        assertEquals(102L, foundItem.getProductId(), "El ID de producto debería coincidir con 102");
        assertEquals(3, foundItem.getQuantity(), "La cantidad debería coincidir con 3");
    }

    @Test
    @DisplayName("Eliminar ítem deseado por ID usando consulta personalizada")
    @Transactional
    public void testDeleteByCustomQuery() {
        wishedItemRepository.deleteByCustomQuery(wishedItem1.getId());

        List<WishedItem> remainingItems = wishedItemRepository.findAll();
        assertEquals(1, remainingItems.size(), "Debería quedar un solo ítem deseado después de la eliminación");
        assertEquals(102L, remainingItems.get(0).getProductId(), "El ítem restante debería tener el ID de producto 102");
    }

    @Test
    @DisplayName("Eliminar todos los ítems deseados por usuario")
    @Transactional
    public void testDeleteAllByUser() {
        wishedItemRepository.deleteAllByUser(user);

        List<WishedItem> remainingItems = wishedItemRepository.findByUser(user);
        assertTrue(remainingItems.isEmpty(), "No debería quedar ningún ítem deseado para el usuario después de la eliminación");
    }

    @Test
    @DisplayName("Actualizar cantidad de ítem deseado por ID de producto y usuario")
    @Transactional
    public void testUpdateQuantityByProductIdAndUser() {
        wishedItemRepository.updateQuantityByProductIdAndUser(101L, 5, user);

        WishedItem updatedItem = wishedItemRepository.findByUserAndProductId(user, 101L);
        assertNotNull(updatedItem, "Debería encontrar el ítem deseado actualizado");
        assertEquals(5, updatedItem.getQuantity(), "La cantidad debería haber sido actualizada a 5");
    }
}
