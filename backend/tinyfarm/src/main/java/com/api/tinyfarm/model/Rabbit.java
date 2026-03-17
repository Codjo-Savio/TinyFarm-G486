package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Rabbit")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rabbit extends Animal {

    public enum RabbitEnumType {
        R("Rabbit"),
        B("Babby Rabbit");

        private String wording;

        RabbitEnumType(String wording) {
            this.wording = wording;
        }

        String getWording() {
            return this.wording;
        }
    }

    // ID NAME RABBITENUMTYPE

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aid")
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "rabbitType")
    private RabbitEnumType rabbitType;
}
