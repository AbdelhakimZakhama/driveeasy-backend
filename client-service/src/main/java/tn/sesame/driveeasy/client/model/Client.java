package tn.sesame.driveeasy.client.model;

import java.time.LocalDate;

public class Client {
    private Long id;
    private String cin;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String email;
    private String telephone;
    private String adresse;
    private String numeroPermis;
    private LocalDate dateExpirationPermis;
    private CategorieClient categorieClient;
    private LocalDate dateInscription;

    public Client() {}

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getCin() { return cin; }
    public void setCin(String v) { this.cin = v; }
    public String getNom() { return nom; }
    public void setNom(String v) { this.nom = v; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String v) { this.prenom = v; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate v) { this.dateNaissance = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String v) { this.telephone = v; }
    public String getAdresse() { return adresse; }
    public void setAdresse(String v) { this.adresse = v; }
    public String getNumeroPermis() { return numeroPermis; }
    public void setNumeroPermis(String v) { this.numeroPermis = v; }
    public LocalDate getDateExpirationPermis() { return dateExpirationPermis; }
    public void setDateExpirationPermis(LocalDate v) { this.dateExpirationPermis = v; }
    public CategorieClient getCategorieClient() { return categorieClient; }
    public void setCategorieClient(CategorieClient v) { this.categorieClient = v; }
    public LocalDate getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDate v) { this.dateInscription = v; }
}