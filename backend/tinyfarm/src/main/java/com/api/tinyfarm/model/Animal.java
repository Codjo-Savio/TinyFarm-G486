package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "Animal")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aid", nullable = false)
    private Long id;

    @Column(name = "uid")
    private Long userId;

    @Column(name = "clean")
    private Boolean clean;

    @Column(name = "healthy")
    private Boolean healthy;

    @Column(name = "age")
    private Integer age;

    @Column(name = "weight")
    private Float weight;

    @Column(name = "fed_today")
    private Boolean fedToday = false;

    @Column(name = "watered_today")
    private Boolean wateredToday = false;

    public enum AnimalGender {
        M("Male"),
        F("Female");

        final String wording;

        AnimalGender(String wording) {
            this.wording = wording;
        }

        String getWording() {
            return this.wording;
        }
    }

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", columnDefinition = "genderenum")
    private AnimalGender gender;

    @PrePersist
    public void prePersist() {
        if (clean == null) {
            clean = true;
        }
        if (healthy == null) {
            healthy = true;
        }
        if (age == null) {
            age = 0;
        }
        if (weight == null) {
            weight = 1f;
        }
        if (fedToday == null) {
            fedToday = true;
        }
        if (wateredToday == null) {
            wateredToday = true;
        }
    }
}
