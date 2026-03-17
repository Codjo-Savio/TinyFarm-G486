package com.api.tinyfarm.model;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chicken")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Chicken extends Animal{

    enum chickenTypeEnum{poussin,poule,coq}

    @Column(name = "chicken_type")
    private chickenTypeEnum chickenType;

    @Column(name = "name")
    private String name;

    @Column(name = "fasting")
    private Integer fasting;
}
