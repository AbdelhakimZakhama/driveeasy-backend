package tn.sesame.driveeasy.vehicule.model;

import java.math.BigDecimal;

public class Vehicule {
    private Long id;
    private String immatriculation;
    private String marque;
    private String modele;
    private int annee;
    private String couleur;
    private Carburant carburant;
    private Boite boite;
    private int nbPlaces;
    private int kilometrage;
    private BigDecimal prixJournalier;
    private Statut statut;
    private String categorie;   // ECONOMIQUE, BERLINE, SUV...
    private String imageUrl;

    public Vehicule() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String v) { this.immatriculation = v; }
    public String getMarque() { return marque; }
    public void setMarque(String v) { this.marque = v; }
    public String getModele() { return modele; }
    public void setModele(String v) { this.modele = v; }
    public int getAnnee() { return annee; }
    public void setAnnee(int v) { this.annee = v; }
    public String getCouleur() { return couleur; }
    public void setCouleur(String v) { this.couleur = v; }
    public Carburant getCarburant() { return carburant; }
    public void setCarburant(Carburant v) { this.carburant = v; }
    public Boite getBoite() { return boite; }
    public void setBoite(Boite v) { this.boite = v; }
    public int getNbPlaces() { return nbPlaces; }
    public void setNbPlaces(int v) { this.nbPlaces = v; }
    public int getKilometrage() { return kilometrage; }
    public void setKilometrage(int v) { this.kilometrage = v; }
    public BigDecimal getPrixJournalier() { return prixJournalier; }
    public void setPrixJournalier(BigDecimal v) { this.prixJournalier = v; }
    public Statut getStatut() { return statut; }
    public void setStatut(Statut v) { this.statut = v; }
    public String getCategorie() { return categorie; }
    public void setCategorie(String v) { this.categorie = v; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String v) { this.imageUrl = v; }
}