package com.api.tinyfarm.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uid", nullable = false)
    private Integer id;

    @Column(name = "name", length = 20)
    private String name;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "ecus")
    private Integer ecus = 1500; // la banque prête 1500 écus au départ

    @Column(name = "level")
    private Integer level = 1; // On commence au niveau 1
}