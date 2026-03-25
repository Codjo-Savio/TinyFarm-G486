package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

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

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StockId implements Serializable {

        @Column(name = "uid")
        private Long uid;

        @Column(name = "productID")
        private Long productID;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            StockId stockId = (StockId) o;
            return Objects.equals(uid, stockId.uid) && Objects.equals(productID, stockId.productID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(uid, productID);
        }
    }
}
