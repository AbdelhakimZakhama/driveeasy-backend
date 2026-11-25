package tn.sesame.driveeasy.location.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.sesame.driveeasy.location.dto.*;
import tn.sesame.driveeasy.location.model.Location;
import tn.sesame.driveeasy.location.model.StatutLocation;
import tn.sesame.driveeasy.location.service.LocationService;

@RestController
@RequestMapping("/locations")
public class LocationController {

    private final LocationService service;

    public LocationController(LocationService service) { this.service = service; }

    @GetMapping
    public PageResponse<Location> list(
            @RequestParam(required = false) StatutLocation statut,
            @RequestParam(required = false) Long clientId,
            @RequestParam(required = false) Long vehiculeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.search(statut, clientId, vehiculeId, dateDebut, dateFin, page, size);
    }

    @GetMapping("/{id}")
    public LocationDetail get(@PathVariable Long id) { return service.detail(id); }

    @PostMapping
    public ResponseEntity<Location> create(@Valid @RequestBody LocationRequest r) {
        Location l = service.create(r);
        return ResponseEntity.created(URI.create("/locations/" + l.getId())).body(l);
    }

    @PostMapping("/simulation")
    public SimulationResponse simulation(@Valid @RequestBody LocationRequest r) {
        return service.simulate(r);
    }

    @PatchMapping("/{id}/depart")
    public Location depart(@PathVariable Long id, @Valid @RequestBody(required = false) DepartRequest r) {
        return service.depart(id, r == null ? null : r.kmDepart());
    }

    @PatchMapping("/{id}/retour")
    public Location retour(@PathVariable Long id, @Valid @RequestBody RetourRequest r) {
        return service.retour(id, r);
    }

    @PatchMapping("/{id}/annulation")
    public Location annulation(@PathVariable Long id) { return service.annuler(id); }

    @GetMapping("/statistiques")
    public StatistiquesResponse statistiques() { return service.statistiques(); }
}