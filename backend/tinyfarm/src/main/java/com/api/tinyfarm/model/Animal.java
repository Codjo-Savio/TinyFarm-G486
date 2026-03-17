package com.api.tinyfarm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Animal")
@Inheritance(strategy = InheritanceType.JOINED)
public class Animal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "a_id")
    private Integer aId;

    @Column(name = "u_id")
    private Integer uId;

    @Column(name = "clean")
    private Boolean clean;

    @Column(name = "healthy")
    private Boolean healthy;

    @Column(name = "age")
    private Integer age;

    @Column(name = "weight")
    private Float weight;

    @Column(name = "a_gender")
    private Boolean aGender;

    public Integer getAId() {
        return aId;
    }

    public void setAId(Integer aId) {
        this.aId = aId;
    }

    public Integer getUId() {
        return uId;
    }

    public void setUId(Integer uId) {
        this.uId = uId;
    }

    public Boolean getClean() {
        return clean;
    }

    public void setClean(Boolean clean) {
        this.clean = clean;
    }

    public Boolean getHealthy() {
        return healthy;
    }

    public void setHealthy(Boolean healthy) {
        this.healthy = healthy;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Boolean getAGender() {
        return aGender;
    }

    public void setAGender(Boolean aGender) {
        this.aGender = aGender;
    }
}
