package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`user`")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uid")
    private Long id;

    public enum Gender {
        M("Masculine"),
        F("Feminine");

        private final String wording;

        Gender(String wording) {
            this.wording = wording;
        }

        String getWording() {
            return this.wording;
        }
    }

    @Column(name = "name", length = 20)
    private String name;

    @Column(name = "email", length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "ecus")
    private Float ecus;

    @Column(name = "hibernation")
    private Boolean hibernation;

    @Column(name = "level")
    private Integer level;

    // default values for ecus and level
    @PrePersist
    public void prePersist() {
        if (this.ecus == null) {
            this.ecus = 1500F;
        }

        if (this.hibernation == null) {
            this.hibernation = false;
        }

        if (this.level == null) {
            this.level = 1;
        }
    }
}
