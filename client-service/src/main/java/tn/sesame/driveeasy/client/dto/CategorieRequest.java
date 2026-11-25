package tn.sesame.driveeasy.client.dto;

import jakarta.validation.constraints.NotNull;
import tn.sesame.driveeasy.client.model.CategorieClient;

public record CategorieRequest(@NotNull CategorieClient categorie) {}