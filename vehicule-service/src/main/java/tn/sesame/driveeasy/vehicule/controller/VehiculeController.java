package tn.sesame.driveeasy.vehicule.controller;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.sesame.driveeasy.vehicule.dto.*;
import tn.sesame.driveeasy.vehicule.exception.*;
import tn.sesame.driveeasy.vehicule.model.*;
import tn.sesame.driveeasy.vehicule.repository.VehiculeRepository;

@RestController
@RequestMapping("/vehicules")
public class VehiculeController {

    private final VehiculeRepository repo;

    public VehiculeController(VehiculeRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Vehicule> list(@RequestParam(required = false) String categorie,
                               @RequestParam(required = false) Carburant carburant,
                               @RequestParam(required = false) Statut statut,
                               @RequestParam(required = false) BigDecimal prixMax) {
        return repo.findAll().stream()
                .filter(v -> categorie == null || categorie.equalsIgnoreCase(v.getCategorie()))
                .filter(v -> carburant == null || v.getCarburant() == carburant)
                .filter(v -> statut == null || v.getStatut() == statut)
                .filter(v -> prixMax == null || v.getPrixJournalier().compareTo(prixMax) <= 0)
                .toList();
    }

    @GetMapping("/disponibles")
    public List<Vehicule> disponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        if (dateFin.isBefore(dateDebut))
            throw new IllegalArgumentException("dateFin must be after dateDebut");
        return repo.findAll().stream().filter(v -> v.getStatut() == Statut.DISPONIBLE).toList();
    }

    @GetMapping("/{id}")
    public Vehicule get(@PathVariable Long id) { return find(id); }

    @PostMapping
    public ResponseEntity<Vehicule> create(@Valid @RequestBody VehiculeRequest r) {
        checkRules(r, null);
        Vehicule v = repo.save(toEntity(new Vehicule(), r));
        return ResponseEntity.created(URI.create("/vehicules/" + v.getId())).body(v);
    }

    @PutMapping("/{id}")
    public Vehicule update(@PathVariable Long id, @Valid @RequestBody VehiculeRequest r) {
        Vehicule existing = find(id);
        checkRules(r, id);
        return repo.save(toEntity(existing, r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Vehicule v = find(id);
        if (v.getStatut() == Statut.LOUE)
            throw new ConflictException("Cannot delete a rented vehicle");
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/statut")
    public Vehicule changeStatut(@PathVariable Long id, @Valid @RequestBody StatutRequest r) {
        Vehicule v = find(id);
        v.setStatut(r.statut());
        return repo.save(v);
    }

    // ---- helpers ----
    private Vehicule find(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Vehicule " + id + " not found"));
    }

    private void checkRules(VehiculeRequest r, Long selfId) {
        if (r.annee() > Year.now().getValue())
            throw new IllegalArgumentException("annee cannot be in the future");
        if (repo.existsByImmatriculation(r.immatriculation(), selfId))
            throw new ConflictException("Immatriculation already exists: " + r.immatriculation());
    }

    private Vehicule toEntity(Vehicule v, VehiculeRequest r) {
        v.setImmatriculation(r.immatriculation()); v.setMarque(r.marque()); v.setModele(r.modele());
        v.setAnnee(r.annee()); v.setCouleur(r.couleur()); v.setCarburant(r.carburant());
        v.setBoite(r.boite()); v.setNbPlaces(r.nbPlaces()); v.setKilometrage(r.kilometrage());
        v.setPrixJournalier(r.prixJournalier());
        v.setStatut(r.statut() != null ? r.statut() : (v.getStatut() != null ? v.getStatut() : Statut.DISPONIBLE));
        v.setCategorie(r.categorie()); v.setImageUrl(r.imageUrl());
        return v;
    }
}