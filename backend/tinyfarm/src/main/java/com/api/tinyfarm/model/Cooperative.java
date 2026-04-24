package com.api.tinyfarm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cooperative")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cooperative {
    @EmbeddedId
    @AttributeOverrides({
        @AttributeOverride(name = "userId", column = @Column(name = "uid")),
        @AttributeOverride(name = "productId", column = @Column(name = "product_id"))
    })
    private CooperativeID cooperativeId;

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

    @Column(name = "price")
    private Float price;

    @Column(name = "quantity")
    private int quantity;

    @Transient
    public Long getUserId() {
        return cooperativeId == null ? null : cooperativeId.getUserId();
    }

    public void setUserId(Long userId) {
        ensureCooperativeId();
        cooperativeId.setUserId(userId);
    }

    @Transient
    public Long getProductId() {
        return cooperativeId == null ? null : cooperativeId.getProductId();
    }

    public void setProductId(Long productId) {
        ensureCooperativeId();
        cooperativeId.setProductId(productId);
    }

    private void ensureCooperativeId() {
        if (cooperativeId == null) {
            cooperativeId = new CooperativeID();
        }
    }
}
