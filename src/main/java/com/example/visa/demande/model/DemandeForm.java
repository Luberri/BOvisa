package com.example.visa.demande.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class DemandeForm {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    private String prenoms;
    private String nomJeuneFille;
    private LocalDate dateNaissance;
    private String lieuNaissance;

    @NotNull(message = "La situation de famille est obligatoire")
    private Integer situationFamilleId;

    @NotNull(message = "La nationalite est obligatoire")
    private Integer nationaliteId;

    private String profession;
    private String telephone;

    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "L'adresse a Madagascar est obligatoire")
    private String adresse;

    @NotBlank(message = "La categorie est obligatoire")
    private String categorieDemande;

    @NotBlank(message = "La reference du visa est obligatoire")
    private String referenceVisa;

    @NotBlank(message = "Le numero du visa est obligatoire")
    private String numeroVisa;

    @NotNull(message = "La date d'entree a Madagascar est obligatoire")
    private LocalDate dateEntreeMada;

    @NotBlank(message = "Le lieu d'entree a Madagascar est obligatoire")
    private String lieuEntreeMada;

    @NotNull(message = "La date d'expiration du visa est obligatoire")
    private LocalDate dateExpirationVisa;

    private Set<Integer> pieceIds = new HashSet<>();

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenoms() {
        return prenoms;
    }

    public void setPrenoms(String prenoms) {
        this.prenoms = prenoms;
    }

    public String getNomJeuneFille() {
        return nomJeuneFille;
    }

    public void setNomJeuneFille(String nomJeuneFille) {
        this.nomJeuneFille = nomJeuneFille;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getLieuNaissance() {
        return lieuNaissance;
    }

    public void setLieuNaissance(String lieuNaissance) {
        this.lieuNaissance = lieuNaissance;
    }

    public Integer getSituationFamilleId() {
        return situationFamilleId;
    }

    public void setSituationFamilleId(Integer situationFamilleId) {
        this.situationFamilleId = situationFamilleId;
    }

    public Integer getNationaliteId() {
        return nationaliteId;
    }

    public void setNationaliteId(Integer nationaliteId) {
        this.nationaliteId = nationaliteId;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getCategorieDemande() {
        return categorieDemande;
    }

    public void setCategorieDemande(String categorieDemande) {
        this.categorieDemande = categorieDemande;
    }

    public String getReferenceVisa() {
        return referenceVisa;
    }

    public void setReferenceVisa(String referenceVisa) {
        this.referenceVisa = referenceVisa;
    }

    public String getNumeroVisa() {
        return numeroVisa;
    }

    public void setNumeroVisa(String numeroVisa) {
        this.numeroVisa = numeroVisa;
    }

    public LocalDate getDateEntreeMada() {
        return dateEntreeMada;
    }

    public void setDateEntreeMada(LocalDate dateEntreeMada) {
        this.dateEntreeMada = dateEntreeMada;
    }

    public String getLieuEntreeMada() {
        return lieuEntreeMada;
    }

    public void setLieuEntreeMada(String lieuEntreeMada) {
        this.lieuEntreeMada = lieuEntreeMada;
    }

    public LocalDate getDateExpirationVisa() {
        return dateExpirationVisa;
    }

    public void setDateExpirationVisa(LocalDate dateExpirationVisa) {
        this.dateExpirationVisa = dateExpirationVisa;
    }

    public Set<Integer> getPieceIds() {
        return pieceIds;
    }

    public void setPieceIds(Set<Integer> pieceIds) {
        this.pieceIds = pieceIds;
    }
}
