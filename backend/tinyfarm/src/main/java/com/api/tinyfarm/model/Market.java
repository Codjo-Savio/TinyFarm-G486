package com.api.tinyfarm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @AttributeOverrides(
        {
            @AttributeOverride(name = "userId", column = @Column(name = "uid")),
            @AttributeOverride(
                name = "productId",
                column = @Column(name = "product_id")
            ),
        }
    )
    private MarketID marketId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "uid",
            referencedColumnName = "uid",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "product_id",
            referencedColumnName = "product_id",
            insertable = false,
            updatable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Product product;

    @Column(name = "unit_price")
    private Float unitPrice;

    @Column(name = "quantity")
    private int quantity;

    @Transient
    public Long getUserId() {
        return marketId == null ? null : marketId.getUserId();
    }

    public void setUserId(Long userId) {
        ensureMarketId();
        marketId.setUserId(userId);
    }

    @Transient
    public Long getProductId() {
        return marketId == null ? null : marketId.getProductId();
    }

    public void setProductId(Long productId) {
        ensureMarketId();
        marketId.setProductId(productId);
    }

    private void ensureMarketId() {
        if (marketId == null) {
            marketId = new MarketID();
        }
    }
}
