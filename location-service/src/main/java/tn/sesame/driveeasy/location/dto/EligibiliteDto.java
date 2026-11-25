package tn.sesame.driveeasy.location.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EligibiliteDto(Long clientId, boolean eligible, String raison) {}