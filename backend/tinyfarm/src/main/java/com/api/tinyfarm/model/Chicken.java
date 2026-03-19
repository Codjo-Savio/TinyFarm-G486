package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "chicken")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chicken extends Animal {

    public enum ChickenType {
        C("Chick"),
        H("Hen"),
        R("Rooster"),
        L("Laying hen"),
        B("Breeding rooster");

        String wording;

        ChickenType(String wording) {
            this.wording = wording;
        }

        String getWording() {
            return this.wording;
        }
    }

    @Column(name = "aid")
    Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "chickenType")
    private ChickenType chickenType;

    @Column(name = "name")
    private String name;

    @Column(name = "fasting_days")
    private Integer fastingDays;

    @Column(name = "sick_days")
    private Integer sickDays;

    @PrePersist
    public void prePersist() {
        this.fastingDays = 0;
        this.sickDays = 0;
    }
}
