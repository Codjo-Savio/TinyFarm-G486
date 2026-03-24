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

    @Column(name = "uid")
    private Long userId;

    @Column(name = "productID")
    private Long productID;

    @Column(name = "price")
    private Float price;
}
