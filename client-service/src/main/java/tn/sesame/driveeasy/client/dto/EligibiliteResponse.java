package tn.sesame.driveeasy.client.dto;

public record EligibiliteResponse(Long clientId, boolean eligible, int age, boolean permisValide, String raison) {}