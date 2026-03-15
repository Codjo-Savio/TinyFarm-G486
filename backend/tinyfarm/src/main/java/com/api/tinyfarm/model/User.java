package com.api.tinyfarm.model;

import jakarta.persistence.*;

@Entity
@Table(name = "`User`")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "u_id")
    private Integer uId;

    @Column(name = "nom", length = 20)
    private String nom;

    @Column(name = "sexe", length = 20)
    private String sexe;

    @Column(name = "ecus")
    private Integer ecus = 1500; // la banque prête 1500 écus au départ

    @Column(name = "level")
    private Integer level = 1; // On commence au niveau 1

    // Getters & Setters
    public Integer getUId() { return uId; }
    public void setUId(Integer uId) { this.uId = uId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getSexe() { return sexe; }
    public void setSexe(String sexe) { this.sexe = sexe; }

    public Integer getEcus() { return ecus; }
    public void setEcus(Integer ecus) { this.ecus = ecus; }

    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
}