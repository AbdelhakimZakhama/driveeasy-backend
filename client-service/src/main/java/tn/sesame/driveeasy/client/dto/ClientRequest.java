package tn.sesame.driveeasy.client.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record ClientRequest(
        @NotBlank String cin,
        @NotBlank String nom,
        @NotBlank String prenom,
        @NotNull @Past LocalDate dateNaissance,
        @NotBlank @Email String email,
        String telephone,
        String adresse,
        @NotBlank String numeroPermis,
        @NotNull LocalDate dateExpirationPermis) {}