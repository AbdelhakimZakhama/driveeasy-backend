package tn.sesame.driveeasy.vehicule.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import tn.sesame.driveeasy.vehicule.model.*;

public record VehiculeRequest(
        @NotBlank String immatriculation,
        @NotBlank String marque,
        @NotBlank String modele,
        @Min(1950) int annee,
        String couleur,
        @NotNull Carburant carburant,
        @NotNull Boite boite,
        @Min(1) int nbPlaces,
        @Min(0) int kilometrage,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal prixJournalier,
        Statut statut,
        String categorie,
        String imageUrl) {}