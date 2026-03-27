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

    @MapsId("userId")
    @JoinColumn(name = "uid")
    @Column(name = "uid", insertable = false, updatable = false) // Déjà géré par MarketID
    private Long userId;

    @MapsId("productID")
    @JoinColumn(name = "productID")
    @Column(name = "productID", insertable = false, updatable = false) // idem
    private Long productId;

    @Column(name = "price") // On garde l'attribut price pour facilité le code.
    private Float price;
}
