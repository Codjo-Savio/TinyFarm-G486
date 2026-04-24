package com.api.tinyfarm.dto;

import lombok.Data;

@Data
public class MarketBuyRequest {

    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private Integer quantity;
}
