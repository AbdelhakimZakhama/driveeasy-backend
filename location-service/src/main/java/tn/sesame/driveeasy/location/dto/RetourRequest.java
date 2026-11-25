package tn.sesame.driveeasy.location.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record RetourRequest(@NotNull @Min(0) Integer kmRetour, LocalDate dateRetour) {}