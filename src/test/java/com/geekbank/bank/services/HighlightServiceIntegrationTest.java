package com.geekbank.bank.services;

import com.geekbank.bank.models.HighlightItem;
import com.geekbank.bank.models.HighlightItemWithGiftcardDTO;
import com.geekbank.bank.repositories.HighlightItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
public class HighlightServiceIntegrationTest {

    @Autowired
    private HighlightService highlightService;

    @Autowired
    private HighlightItemRepository highlightItemRepository;

    @Autowired
    private KinguinService kinguinService;

    @BeforeEach
    public void setUp() {
        highlightItemRepository.deleteAll();
    }

    @Test
    @DisplayName("Agregar nuevos highlights y verificar que se guarden correctamente")
    public void testAddHighlightItems() {
        List<Long> productIds = Arrays.asList(1L, 2L, 3L);

        List<HighlightItem> addedHighlights = highlightService.addHighlightItems(productIds);

        assertEquals(3, addedHighlights.size(), "Deberían haberse guardado tres elementos destacados");
        addedHighlights.forEach(highlightItem -> assertTrue(productIds.contains(highlightItem.getProductId()), "El ID de producto debería coincidir"));
    }

    @Test
    @DisplayName("Obtener highlights con giftcards")
    public void testGetHighlightsByProductIds() {
        // Preparar datos de prueba
        HighlightItem highlightItem = new HighlightItem();
        highlightItem.setProductId(1L);
        highlightItemRepository.save(highlightItem);

        // Simular la respuesta del servicio externo
        when(kinguinService.fetchGiftCardById(anyString()))
                .thenReturn(null);  // Devuelve null en lugar de Optional.empty()

        List<HighlightItemWithGiftcardDTO> highlightsWithGiftcards = highlightService.getHighlightsByProductIds();

        assertEquals(1, highlightsWithGiftcards.size(), "Debería haber un highlight en la lista");
        assertEquals(1L, highlightsWithGiftcards.get(0).getHighlightItem().getProductId(), "El ID de producto del highlight debería coincidir");
        assertNull(highlightsWithGiftcards.get(0).getGiftcard(), "El giftcard debería ser nulo si no se encuentra en el servicio externo");
    }

    @Test
    @DisplayName("Eliminar highlights por IDs de productos")
    public void testRemoveHighlightItems() {
        // Agregar elementos destacados
        List<Long> productIds = Arrays.asList(1L, 2L, 3L);
        highlightService.addHighlightItems(productIds);

        // Verificar que existen antes de eliminarlos
        assertEquals(3, highlightItemRepository.count(), "Deberían haber tres elementos destacados antes de la eliminación");

        // Ejecutar la eliminación
        highlightService.removeHighlightItems(productIds);

        // Verificar que se hayan eliminado
        List<HighlightItem> remainingItems = highlightItemRepository.findByProductIdIn(productIds);
        assertTrue(remainingItems.isEmpty(), "No deberían quedar elementos destacados después de la eliminación");
    }
}
