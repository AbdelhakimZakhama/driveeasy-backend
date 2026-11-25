package tn.sesame.driveeasy.client.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.sesame.driveeasy.client.dto.*;
import tn.sesame.driveeasy.client.exception.*;
import tn.sesame.driveeasy.client.model.*;
import tn.sesame.driveeasy.client.repository.ClientRepository;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientRepository repo;
    private final int ageMinimum;

    public ClientController(ClientRepository repo,
                            @Value("${driveeasy.client.age-minimum:21}") int ageMinimum) {
        this.repo = repo;
        this.ageMinimum = ageMinimum;   // comes from client-service.yml (Config Server)
    }

    @GetMapping
    public List<Client> search(@RequestParam(required = false) String nom,
                               @RequestParam(required = false) String cin,
                               @RequestParam(required = false) String email) {
        return repo.findAll().stream()
                .filter(c -> nom == null || c.getNom().toLowerCase().contains(nom.toLowerCase())
                        || c.getPrenom().toLowerCase().contains(nom.toLowerCase()))
                .filter(c -> cin == null || c.getCin().equalsIgnoreCase(cin))
                .filter(c -> email == null || c.getEmail().equalsIgnoreCase(email))
                .toList();
    }

    @GetMapping("/{id}")
    public Client get(@PathVariable Long id) { return find(id); }

    @PostMapping
    public ResponseEntity<Client> create(@Valid @RequestBody ClientRequest r) {
        checkUnique(r, null);
        Client c = toEntity(new Client(), r);
        c.setCategorieClient(CategorieClient.STANDARD);
        c.setDateInscription(LocalDate.now());
        repo.save(c);
        return ResponseEntity.created(URI.create("/clients/" + c.getId())).body(c);
    }

    @PutMapping("/{id}")
    public Client update(@PathVariable Long id, @Valid @RequestBody ClientRequest r) {
        Client existing = find(id);
        checkUnique(r, id);
        return repo.save(toEntity(existing, r));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        find(id);
        // TODO: refuse (409) if the client has an active rental: needs a call to location-service
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/eligibilite")
    public EligibiliteResponse eligibilite(@PathVariable Long id) {
        Client c = find(id);
        LocalDate today = LocalDate.now();
        int age = Period.between(c.getDateNaissance(), today).getYears();
        boolean permisValide = c.getDateExpirationPermis().isAfter(today);
        boolean ageOk = age >= ageMinimum;
        String raison = ageOk && permisValide ? "OK"
                : !ageOk && !permisValide ? "Age < " + ageMinimum + " and licence expired"
                : !ageOk ? "Age < " + ageMinimum : "Licence expired";
        return new EligibiliteResponse(id, ageOk && permisValide, age, permisValide, raison);
    }

    @PatchMapping("/{id}/categorie")
    public Client changeCategorie(@PathVariable Long id, @Valid @RequestBody CategorieRequest r) {
        Client c = find(id);
        c.setCategorieClient(r.categorie());
        return repo.save(c);
    }

    // ---- helpers ----
    private Client find(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Client " + id + " not found"));
    }

    private void checkUnique(ClientRequest r, Long selfId) {
        if (repo.existsByCin(r.cin(), selfId)) throw new ConflictException("CIN already exists: " + r.cin());
        if (repo.existsByEmail(r.email(), selfId)) throw new ConflictException("Email already exists: " + r.email());
        if (repo.existsByNumeroPermis(r.numeroPermis(), selfId))
            throw new ConflictException("Licence number already exists: " + r.numeroPermis());
    }

    private Client toEntity(Client c, ClientRequest r) {
        c.setCin(r.cin()); c.setNom(r.nom()); c.setPrenom(r.prenom());
        c.setDateNaissance(r.dateNaissance()); c.setEmail(r.email());
        c.setTelephone(r.telephone()); c.setAdresse(r.adresse());
        c.setNumeroPermis(r.numeroPermis()); c.setDateExpirationPermis(r.dateExpirationPermis());
        return c;
    }
}