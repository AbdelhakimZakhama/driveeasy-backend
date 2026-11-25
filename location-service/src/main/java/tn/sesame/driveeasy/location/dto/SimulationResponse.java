package tn.sesame.driveeasy.location.dto;

import java.math.BigDecimal;

public record SimulationResponse(long jours, BigDecimal prixJournalier, BigDecimal coefficientCategorie,
                                 BigDecimal remise, BigDecimal montantTotal) {}