package com.api.tinyfarm.dto;

import lombok.Data;

@Data
public class CooperativeSaleRequest {

    private Long sellerId;
    private Long productId;
    private Integer quantity;
}
