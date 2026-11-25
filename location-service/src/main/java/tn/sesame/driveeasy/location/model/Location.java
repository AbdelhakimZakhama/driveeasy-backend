package tn.sesame.driveeasy.location.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Location {
    private Long id;
    private Long clientId;
    private Long vehiculeId;
    private LocalDate dateDebut;
    private LocalDate dateFinPrevue;
    private LocalDate dateRetourEffectif;
    private Integer kmDepart;
    private Integer kmRetour;
    private BigDecimal prixJournalierApplique;
    private BigDecimal montantTotal;
    private BigDecimal penalite = BigDecimal.ZERO;
    private StatutLocation statut;
    private LocalDateTime dateCreation;

    public Location() {}

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long v) { this.clientId = v; }
    public Long getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(Long v) { this.vehiculeId = v; }
    public LocalDate getDateDebut() { return dateDebut; }
    public void setDateDebut(LocalDate v) { this.dateDebut = v; }
    public LocalDate getDateFinPrevue() { return dateFinPrevue; }
    public void setDateFinPrevue(LocalDate v) { this.dateFinPrevue = v; }
    public LocalDate getDateRetourEffectif() { return dateRetourEffectif; }
    public void setDateRetourEffectif(LocalDate v) { this.dateRetourEffectif = v; }
    public Integer getKmDepart() { return kmDepart; }
    public void setKmDepart(Integer v) { this.kmDepart = v; }
    public Integer getKmRetour() { return kmRetour; }
    public void setKmRetour(Integer v) { this.kmRetour = v; }
    public BigDecimal getPrixJournalierApplique() { return prixJournalierApplique; }
    public void setPrixJournalierApplique(BigDecimal v) { this.prixJournalierApplique = v; }
    public BigDecimal getMontantTotal() { return montantTotal; }
    public void setMontantTotal(BigDecimal v) { this.montantTotal = v; }
    public BigDecimal getPenalite() { return penalite; }
    public void setPenalite(BigDecimal v) { this.penalite = v; }
    public StatutLocation getStatut() { return statut; }
    public void setStatut(StatutLocation v) { this.statut = v; }
    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime v) { this.dateCreation = v; }
}