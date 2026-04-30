package com.api.tinyfarm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
            @AttributeOverride(name = "userId", column = @Column(name = "uid", nullable = false)),
            @AttributeOverride(name = "productId", column = @Column(name = "product_id", nullable = false))
    })
    private StockId id;

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

    @Transient
    public Long getUserId() {
        return id == null ? null : id.getUserId();
    }

    public void setUserId(Long userId) {
        ensureId();
        id.setUserId(userId);
    }

    @Transient
    public Long getProductId() {
        return id == null ? null : id.getProductId();
    }

    public void setProductId(Long productId) {
        ensureId();
        id.setProductId(productId);
    }

    private void ensureId() {
        if (id == null) {
            id = new StockId();
        }
    }
}
