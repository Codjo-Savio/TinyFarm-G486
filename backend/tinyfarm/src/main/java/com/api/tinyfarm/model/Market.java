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
    @AttributeOverrides({
        @AttributeOverride(name = "userId", column = @Column(name = "uid")),
        @AttributeOverride(name = "productID", column = @Column(name = "productID"))
    })
    private MarketID marketId;

    @Column(name = "uid", insertable = false, updatable = false)
    private Long userId;

    @Column(name = "productID", insertable = false, updatable = false)
    private Long productId;

    @Column(name = "price")
    private Float price;
}
