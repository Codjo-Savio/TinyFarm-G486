package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

        private String wording;

        CowType(String wording) {
            this.wording = wording;
        }

        String getWording() {
            return this.wording;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aid")
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "cowType")
    private CowType cowType;

    @Column(name = "milking")
    private Boolean milking;

    @PrePersist
    public void prePersist() {
        this.milking = false;
    }
}
