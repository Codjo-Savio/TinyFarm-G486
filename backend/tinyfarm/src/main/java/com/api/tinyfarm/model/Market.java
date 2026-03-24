package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "market")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Market {

    @EmbeddedId
    private MarketID id;

    @MapsId("uid")
    @JoinColumn(name = "uid")
    private Long userId;

    @MapsId("productID")
    @JoinColumn(name = "productID")
    private Long productId;

    @Column(name = "price")
    private Float price;
}
