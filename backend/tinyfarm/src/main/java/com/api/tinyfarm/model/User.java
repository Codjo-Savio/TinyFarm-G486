package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    @Column(name = "hibernation_date")
    private LocalDateTime hibernationDate;

    @Column(name = "level")
    private Integer level;

    @Column(name = "remaining_purchases")
    private Integer remainingPurchases;

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

        if (this.remainingPurchases== null) {
            this.remainingPurchases= 12;
        }
    }
}
