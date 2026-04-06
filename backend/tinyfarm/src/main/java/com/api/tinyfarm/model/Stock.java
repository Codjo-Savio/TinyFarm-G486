package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    @EmbeddedId
    private StockId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("uid")
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("productID")
    @JoinColumn(name = "productID", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "collectible", nullable = false)
    private Boolean collectible = false;

    @PrePersist
    private void prePersist() {
        if (quantity == null) {
            quantity = 0;
        }
        if (collectible == null) {
            collectible = false;
        }
    }
}
