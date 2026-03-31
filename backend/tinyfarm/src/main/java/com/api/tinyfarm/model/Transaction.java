package com.api.tinyfarm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import javax.annotation.processing.Generated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`transaction`")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @Column(name = "tid")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @JoinColumn(name = "seller")
    Long seller;

    @JoinColumn(name = "buyer")
    Long buyer;

    @JoinColumn(name = "product")
    Long product;

    @Column(name = "quantity")
    Integer quantity;

    @Column(name = "totalPrice")
    float totalPrice;

    @Column(name = "transactionDate")
    LocalDateTime transactionDate;

    @PrePersist
    public void prePersist() {
        this.transactionDate = LocalDateTime.now();
    }
}
