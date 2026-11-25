package tn.sesame.driveeasy.vehicule.dto;

import jakarta.validation.constraints.NotNull;
import tn.sesame.driveeasy.vehicule.model.Statut;

public record StatutRequest(@NotNull Statut statut) {}