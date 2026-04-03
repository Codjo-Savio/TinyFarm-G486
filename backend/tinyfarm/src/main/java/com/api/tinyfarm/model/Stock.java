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
    @AttributeOverrides({
        @AttributeOverride(name = "uid", column = @Column(name = "uid", nullable = false)),
        @AttributeOverride(name = "productID", column = @Column(name = "productID", nullable = false))
    })
    private StockId id;

    @Column(name = "uid", nullable = false, insertable = false, updatable = false)
    private Long userId;

    @Column(name = "productID", nullable = false, insertable = false, updatable = false)
    private Long productId;

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
