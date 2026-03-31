package com.api.tinyfarm.model;

import jakarta.persistence.*;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarketID implements Serializable {

    private Long userId;
    private Long productID;
}
