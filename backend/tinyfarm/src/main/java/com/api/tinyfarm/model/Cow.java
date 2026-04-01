package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "cow")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cow extends Animal {

    public enum CowType {
        D("Dairy"),
        B("Beef"),
        C("Calf");

        private final String wording;

        CowType(String wording) {
            this.wording = wording;
        }

        String getWording() {
            return this.wording;
        }
    }

    @MapsId
    @Column(name = "aid")
    private Long id;

    @Column(name = "name", length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "cowType")
    private CowType cowType;

    @Column(name = "milking")
    private Boolean milking;

    @PrePersist
    @Override
    public void prePersist() {
        super.prePersist();
        this.milking = false;
    }
}
