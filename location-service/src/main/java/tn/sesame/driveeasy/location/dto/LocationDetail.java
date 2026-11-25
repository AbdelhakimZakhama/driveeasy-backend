package tn.sesame.driveeasy.location.dto;

import tn.sesame.driveeasy.location.model.Location;

public record LocationDetail(Location location, ClientDto client, VehiculeDto vehicule) {}