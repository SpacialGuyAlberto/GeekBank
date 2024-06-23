package com.geekbank.bank.models;
import jakarta.persistence.*;
@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    private Long productId;
    private int quantity;

    public Long getId(){
        return this.id;
    }
    public void setId(Long id){
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }


    // Getters and Setters

}