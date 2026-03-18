package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rabbit")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rabbit extends Animal {

    public enum RabbitTypeEnum {
        lapereau,
        lapin,
    }

    @Column(name = "name", length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "rabbitType")
    private RabbitTypeEnum rabbitType;
}
