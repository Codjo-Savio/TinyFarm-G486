package com.api.tinyfarm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transaction")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @Column(name = "tid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "seller")
    private Long seller;

    @Column(name = "buyer")
    private Long buyer;

    @Column(name = "product")
    private Long product;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "totalPrice")
    private float totalPrice;

    @Column(name = "transactionDate")
    private LocalDateTime transactionDate;

    @PrePersist
    public void prePersist() {
        this.transactionDate = LocalDateTime.now();
    }
}
