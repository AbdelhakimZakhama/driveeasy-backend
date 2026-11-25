package tn.sesame.driveeasy.location.dto;

import java.math.BigDecimal;
import java.util.List;

public record StatistiquesResponse(BigDecimal chiffreAffaires, BigDecimal chiffreAffairesMois,
                                   long locationsEnCours, long locationsTerminees,
                                   double tauxOccupationPourcent, List<TopVehicule> topVehicules) {

    public record TopVehicule(Long vehiculeId, String vehicule, long nombreLocations) {}
}