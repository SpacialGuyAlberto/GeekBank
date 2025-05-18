package com.geekbank.bank.giftcard.model;

import com.geekbank.bank.giftcard.constants.FreeFireDiamondDenomination;
import jakarta.persistence.*;

@Entity
@DiscriminatorValue("FreeFireDiamondProduct")
public class FreeFireDiamondProduct extends Product {

    @Enumerated(EnumType.STRING)
    @Column(name = "denomination")
    private FreeFireDiamondDenomination denomination;
    public FreeFireDiamondProduct() {}

    public FreeFireDiamondProduct(String name, FreeFireDiamondDenomination denomination, String description) {
        super();
        this.denomination = denomination;
    }

    public FreeFireDiamondDenomination getDenomination() {
        return denomination;
    }

    public void setDenomination(FreeFireDiamondDenomination denomination) {
        this.denomination = denomination;
        this.setPrice(denomination.getPrice());
    }

    @Override
    public double getPrice() {
        if (denomination != null) {
            return denomination.getPrice();
        } else {
            return super.getPrice();
        }
    }

    public int getQuantity() {
        return denomination != null ? denomination.getQuantity() : 0;
    }
}
