package com.api.tinyfarm.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "cow")
@PrimaryKeyJoinColumn(name = "aid")
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

    @NotNull(message = "The name is obligatory")
    @Column(name = "name", length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "cow_type")
    private CowType cowType;

    @Column(name = "milking")
    private Boolean milking;

    @PrePersist
    @Override
    public void prePersist() {
        super.prePersist();
        if (this.milking == null) {
            this.milking = false;
        }
        if (this.cowType == null) {
            this.cowType = CowType.C;
        }
    }
}
