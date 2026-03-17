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

public class Chicken extends Animal{

    public enum ChickenType{
        C("Chick"),
        H("Hen"),
        R("Rooster");

        String wording;

        ChickenType(String wording){
            this.wording = wording;
        }

        String getWording(){
            return  this.wording;
        }
    }

    @Column(name = "aid")
    Long id;

    @Column(name = "chickenType")
    private ChickenType chickenType;

    @Column(name = "name")
    private String name;

    @Column(name = "fasting")
    private Boolean fasting;

    @PrePersist
    public void prePersist(){
        this.fasting = false;
    }
}
