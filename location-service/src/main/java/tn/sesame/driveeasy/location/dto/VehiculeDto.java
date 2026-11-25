package tn.sesame.driveeasy.location.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VehiculeDto(Long id, String marque, String modele, int kilometrage,
                          BigDecimal prixJournalier, String statut, String categorie) {}