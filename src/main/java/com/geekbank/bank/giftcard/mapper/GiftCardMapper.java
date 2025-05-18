package com.geekbank.bank.giftcard.mapper;

import com.geekbank.bank.giftcard.model.GiftCardEntity;
import com.geekbank.bank.giftcard.kinguin.model.KinguinGiftCard;

public class GiftCardMapper {

    /**
     * Mapea un objeto KinguinGiftCard a GiftCardEntity.
     *
     * @param kinguinGiftCard Objeto externo de Kinguin.
     * @return Entidad lista para persistir en la base de datos.
     */
    public static GiftCardEntity mapToGiftCardEntity(KinguinGiftCard kinguinGiftCard) {
        if (kinguinGiftCard == null) {
            return null;
        }

        GiftCardEntity entity = new GiftCardEntity();

        // Mapear campos comunes
        entity.setKinguinId((long) kinguinGiftCard.getKinguinId());
        entity.setProductId(truncate(kinguinGiftCard.getProductId(), 255));
        entity.setName(kinguinGiftCard.getName());
        entity.setDescription(kinguinGiftCard.getDescription());
        entity.setPrice(kinguinGiftCard.getPrice());
        entity.setReleaseDate(kinguinGiftCard.getReleaseDate());
        entity.setPlatform(truncate(kinguinGiftCard.getPlatform(), 100));
        entity.setQty(kinguinGiftCard.getQty());
        entity.setTextQty(kinguinGiftCard.getTextQty());
        entity.setRegionalLimitations(truncate(kinguinGiftCard.getRegionalLimitations(), 500));
        entity.setRegionId(kinguinGiftCard.getRegionId());
        entity.setActivationDetails(kinguinGiftCard.getActivationDetails());
        entity.setOriginalName(truncate(kinguinGiftCard.getOriginalName(), 500));
        entity.setOffersCount(kinguinGiftCard.getOffersCount());
        entity.setTotalQty(kinguinGiftCard.getTotalQty());
        entity.setAgeRating(truncate(kinguinGiftCard.getAgeRating(), 100));

        // Mapear géneros
        entity.setGenres(kinguinGiftCard.getGenres());

        return entity;
    }

    /**
     * Actualiza una GiftCardEntity existente con los datos de KinguinGiftCard.
     *
     * @param entity          Entidad existente en la base de datos.
     * @param kinguinGiftCard Objeto externo de Kinguin con datos actualizados.
     */
    public static void updateGiftCardEntity(GiftCardEntity entity, KinguinGiftCard kinguinGiftCard) {
        if (entity == null || kinguinGiftCard == null) {
            return;
        }

        // Actualizar campos comunes
        entity.setProductId(truncate(kinguinGiftCard.getProductId(), 255));
        entity.setName(kinguinGiftCard.getName());
        entity.setDescription(kinguinGiftCard.getDescription());
        entity.setPrice(kinguinGiftCard.getPrice());
        entity.setReleaseDate(kinguinGiftCard.getReleaseDate());
        entity.setPlatform(truncate(kinguinGiftCard.getPlatform(), 100));
        entity.setQty(kinguinGiftCard.getQty());
        entity.setTextQty(kinguinGiftCard.getTextQty());
        entity.setRegionalLimitations(truncate(kinguinGiftCard.getRegionalLimitations(), 500));
        entity.setRegionId(kinguinGiftCard.getRegionId());
        entity.setActivationDetails(kinguinGiftCard.getActivationDetails());
        entity.setOriginalName(truncate(kinguinGiftCard.getOriginalName(), 500));
        entity.setOffersCount(kinguinGiftCard.getOffersCount());
        entity.setTotalQty(kinguinGiftCard.getTotalQty());
        entity.setAgeRating(truncate(kinguinGiftCard.getAgeRating(), 100));
        entity.setDevelopers(kinguinGiftCard.getDevelopers());
        entity.setPublishers(kinguinGiftCard.getPublishers());
        entity.setPlatform(kinguinGiftCard.getPlatform());

        entity.setGenres(kinguinGiftCard.getGenres());
    }

    private static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
