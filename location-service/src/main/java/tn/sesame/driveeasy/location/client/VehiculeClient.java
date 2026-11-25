package tn.sesame.driveeasy.location.client;

import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import tn.sesame.driveeasy.location.dto.*;

@FeignClient(name = "vehicule-service")   // resolved through Eureka, load balanced
public interface VehiculeClient {

    @GetMapping("/vehicules")
    List<VehiculeDto> list();

    @GetMapping("/vehicules/{id}")
    VehiculeDto getVehicule(@PathVariable("id") Long id);

    @PatchMapping("/vehicules/{id}/statut")
    VehiculeDto changeStatut(@PathVariable("id") Long id, @RequestBody StatutRequest request);
}