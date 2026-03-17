package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Animal")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "a_id")
    private Long id;

    @Column(name = "u_id")
    private Long userId;

    @Column(name = "clean")
    private Boolean clean;

    @Column(name = "healthy")
    private Boolean healthy;

    @Column(name = "age")
    private Integer age;

    @Column(name = "weight")
    private Float weight;

    public enum AnimalGender {
        M("Male"),
        F("Female");
        String wording;

        AnimalGender(String wording){
            this.wording = wording;
        }

        String getWording(){
            return  this.wording;
        }
    }

    @Column(name = "gender")
    private AnimalGender gender;
}
