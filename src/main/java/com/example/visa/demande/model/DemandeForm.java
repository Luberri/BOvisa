package com.example.visa.demande.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class DemandeForm {

    private String nom;

    private String prenoms;
    private String nomJeuneFille;
    private LocalDate dateNaissance;
    private String lieuNaissance;

    private Integer situationFamilleId;

    private Integer nationaliteId;

    private String profession;
    private String telephone;

    private String email;

    private String adresse;

    private String numeroPasseport;

    private LocalDate dateDelivrancePasseport;
    private LocalDate dateExpirationPasseport;

    private String categorieDemande;

    private String typeDemande;

    private Boolean avecDonneesAnterieures;

    private String numeroCarteResident;

    private String referenceVisa;

    private String numeroVisa;

    private LocalDate dateEntreeMada;

    private String lieuEntreeMada;

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

    public String getTypeDemande() {
        return typeDemande;
    }

    public void setTypeDemande(String typeDemande) {
        this.typeDemande = typeDemande;
    }

    public Boolean getAvecDonneesAnterieures() {
        return avecDonneesAnterieures;
    }

    public void setAvecDonneesAnterieures(Boolean avecDonneesAnterieures) {
        this.avecDonneesAnterieures = avecDonneesAnterieures;
    }

    public String getNumeroCarteResident() {
        return numeroCarteResident;
    }

    public void setNumeroCarteResident(String numeroCarteResident) {
        this.numeroCarteResident = numeroCarteResident;
    }

    public String getNumeroPasseport() {
        return numeroPasseport;
    }

    public void setNumeroPasseport(String numeroPasseport) {
        this.numeroPasseport = numeroPasseport;
    }

    public LocalDate getDateDelivrancePasseport() {
        return dateDelivrancePasseport;
    }

    public void setDateDelivrancePasseport(LocalDate dateDelivrancePasseport) {
        this.dateDelivrancePasseport = dateDelivrancePasseport;
    }

    public LocalDate getDateExpirationPasseport() {
        return dateExpirationPasseport;
    }

    public void setDateExpirationPasseport(LocalDate dateExpirationPasseport) {
        this.dateExpirationPasseport = dateExpirationPasseport;
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
