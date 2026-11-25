package tn.sesame.driveeasy.location.dto;

import jakarta.validation.constraints.Min;

public record DepartRequest(@Min(0) Integer kmDepart) {}