package tn.sesame.driveeasy.location.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ClientDto(Long id, String nom, String prenom, String categorieClient) {}