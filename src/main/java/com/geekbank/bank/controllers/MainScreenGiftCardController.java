package com.geekbank.bank.controllers;

import com.geekbank.bank.models.MainScreenGiftCardItem;
import com.geekbank.bank.models.MainScreenGiftCardItemDTO;
import com.geekbank.bank.services.MainScreenGiftCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/main-screen-gift-cards")
public class MainScreenGiftCardController {

    private final MainScreenGiftCardService mainScreenGiftCardService;

    @Autowired
    public MainScreenGiftCardController(MainScreenGiftCardService mainScreenGiftCardService) {
        this.mainScreenGiftCardService = mainScreenGiftCardService;
    }

    /**
     * Obtiene todos los elementos de tarjetas de regalo para la pantalla principal con sus detalles.
     *
     * @return Lista de MainScreenGiftCardItemDTO
     */
    @GetMapping
    public ResponseEntity<List<MainScreenGiftCardItemDTO>> getMainScreenGiftCardItems() {
        List<MainScreenGiftCardItemDTO> items = mainScreenGiftCardService.getMainScreenGiftCardItems();
        return ResponseEntity.ok(items);
    }

    /**
     * Agrega nuevos elementos de tarjetas de regalo para la pantalla principal.
     *
     * @param request Objeto que contiene la lista de productIds a agregar
     * @return Lista de MainScreenGiftCardItem agregados
     */
    @PostMapping
    public ResponseEntity<List<MainScreenGiftCardItem>> addMainScreenGiftCardItems(@RequestBody MainScreenGiftCardRequest request) {
        List<MainScreenGiftCardItem> addedItems = mainScreenGiftCardService.addItems(request.getProductIds());
        return ResponseEntity.ok(addedItems);
    }

    /**
     * Elimina elementos de tarjetas de regalo para la pantalla principal basados en una lista de productIds.
     *
     * @param productIds Lista de IDs de productos a eliminar
     * @return Respuesta sin contenido
     */
    @DeleteMapping
    public ResponseEntity<Void> removeMainScreenGiftCardItems(@RequestBody List<Long> productIds) {
        mainScreenGiftCardService.removeItems(productIds);
        return ResponseEntity.noContent().build();
    }

    /**
     * Clase para encapsular la lista de productIds en la solicitud de agregar elementos.
     */
    public static class MainScreenGiftCardRequest {
        private List<Long> productIds;

        public List<Long> getProductIds() {
            return productIds;
        }

        public void setProductIds(List<Long> productIds) {
            this.productIds = productIds;
        }
    }
}