package com.geekbank.bank.combo.dto;

import lombok.Data;
import java.util.List;

@Data
public class ComboRequest {
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private Boolean isActive;
    private List<Long> productIds;
}
