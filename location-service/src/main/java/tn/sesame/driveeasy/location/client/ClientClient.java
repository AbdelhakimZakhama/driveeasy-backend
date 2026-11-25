package tn.sesame.driveeasy.location.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import tn.sesame.driveeasy.location.dto.*;

@FeignClient(name = "client-service")
public interface ClientClient {

    @GetMapping("/clients/{id}")
    ClientDto getClient(@PathVariable("id") Long id);

    @GetMapping("/clients/{id}/eligibilite")
    EligibiliteDto eligibilite(@PathVariable("id") Long id);

    @PatchMapping("/clients/{id}/categorie")
    ClientDto changeCategorie(@PathVariable("id") Long id, @RequestBody CategorieRequest request);
}