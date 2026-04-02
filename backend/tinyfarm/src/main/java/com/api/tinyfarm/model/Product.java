package com.api.tinyfarm.model;

import javax.annotation.processing.Generated;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`product`")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {


    @Id
    @Column(name = "productID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotNull
    @Column(name = "description")
    String description;

    @Column(name = "collection")
    Boolean collectible;

    @Column(name = "price")
    Float price;

    @Column(name = "coef")
    Integer coefficient;

    @PrePersist
    public void prePersist() {
        if (this.coefficient == null) {
            this.coefficient = 1;
        }
        if (this.collectible == null) {
            this.collectible = false;
        }
    }
}
