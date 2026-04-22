package com.api.tinyfarm.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "Product can not be null")
    @Column(name = "product")
    private Long product;

    @NotNull(message = "Quantity can not be null")
    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "total_price")
    private float totalPrice;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @PrePersist
    public void prePersist() {
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
    }
}
