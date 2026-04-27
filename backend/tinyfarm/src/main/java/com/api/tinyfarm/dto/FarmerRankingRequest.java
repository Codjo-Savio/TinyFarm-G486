package com.api.tinyfarm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmerRankingRequest {
    private Long uid;
    private String name;

    private Double production;
    private Double capacity;
    private Double ecus;

    private Double score;
    private Integer rank;
}
