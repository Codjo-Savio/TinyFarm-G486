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
    private MarketID marketId;

    @MapsId("uid")
    @JoinColumn(name = "uid")
    private Long userId;

    @MapsId("productID")
    @JoinColumn(name = "productID")
    private Long productId;

    @Column(name = "price") // A voir si besoin de garder comme on a déjà le prix dans Product
    private Float price;
}
