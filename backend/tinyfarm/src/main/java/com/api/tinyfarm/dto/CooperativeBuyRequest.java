package com.api.tinyfarm.dto;

import lombok.Data;

@Data
public class CooperativeBuyRequest {
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private Integer quantity;
}
