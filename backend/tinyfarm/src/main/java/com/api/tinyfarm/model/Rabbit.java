package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
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
