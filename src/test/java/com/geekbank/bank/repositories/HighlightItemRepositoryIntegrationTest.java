package com.geekbank.bank.repositories;

import com.geekbank.bank.giftcard.featured.highlight.repository.HighlightItemRepository;
import com.geekbank.bank.giftcard.featured.highlight.model.HighlightItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class HighlightItemRepositoryIntegrationTest {

    @Autowired
    private HighlightItemRepository highlightItemRepository;

    private HighlightItem highlightItem1;
    private HighlightItem highlightItem2;
    private HighlightItem highlightItem3;

    @BeforeEach
    public void setup() {
        highlightItemRepository.deleteAll();

        // Crear y guardar elementos destacados (HighlightItems) de prueba
        highlightItem1 = new HighlightItem();
        highlightItem1.setProductId(101L);

        highlightItem2 = new HighlightItem();
        highlightItem2.setProductId(102L);

        highlightItem3 = new HighlightItem();
        highlightItem3.setProductId(103L);

        highlightItemRepository.save(highlightItem1);
        highlightItemRepository.save(highlightItem2);
        highlightItemRepository.save(highlightItem3);
    }

    @Test
    @DisplayName("Buscar elementos destacados por lista de IDs de producto")
    public void testFindByProductIdIn() {
        List<Long> productIds = Arrays.asList(101L, 103L);

        List<HighlightItem> foundItems = highlightItemRepository.findByProductIdIn(productIds);

        assertEquals(2, foundItems.size(), "Debería encontrar dos elementos destacados para los IDs de producto proporcionados");
        assertTrue(foundItems.stream().anyMatch(item -> item.getProductId().equals(101L)), "Debería contener el producto con ID 101");
        assertTrue(foundItems.stream().anyMatch(item -> item.getProductId().equals(103L)), "Debería contener el producto con ID 103");
    }

    @Test
    @DisplayName("Eliminar elementos destacados por lista de IDs de producto")
    public void testDeleteByProductIdIn() {
        List<Long> productIdsToDelete = Arrays.asList(101L, 102L);

        highlightItemRepository.deleteByProductIdIn(productIdsToDelete);

        List<HighlightItem> remainingItems = highlightItemRepository.findAll();
        assertEquals(1, remainingItems.size(), "Debería quedar un solo elemento después de la eliminación");
        assertEquals(103L, remainingItems.get(0).getProductId(), "El producto restante debería tener el ID 103");
    }

    @Test
    @DisplayName("Eliminar todos los elementos destacados")
    public void testDeleteAll() {
        highlightItemRepository.deleteAll();

        List<HighlightItem> remainingItems = highlightItemRepository.findAll();
        assertTrue(remainingItems.isEmpty(), "No debería quedar ningún elemento después de eliminar todos los registros");
    }

    @Test
    @DisplayName("Buscar todos los elementos destacados")
    public void testFindAll() {
        List<HighlightItem> allItems = highlightItemRepository.findAll();

        assertEquals(3, allItems.size(), "Debería encontrar tres elementos destacados en la base de datos");
        assertTrue(allItems.contains(highlightItem1), "Debería contener el primer elemento destacado");
        assertTrue(allItems.contains(highlightItem2), "Debería contener el segundo elemento destacado");
        assertTrue(allItems.contains(highlightItem3), "Debería contener el tercer elemento destacado");
    }
}
