package com.api.tinyfarm.dto;

import lombok.Data;

@Data
public class PublishProductToTradeRequest {
    private Long productId;
    private Integer quantity;
    private float unitPrice;
}
