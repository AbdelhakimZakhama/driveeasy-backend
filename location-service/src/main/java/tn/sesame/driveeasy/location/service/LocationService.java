package tn.sesame.driveeasy.location.service;

import feign.FeignException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.sesame.driveeasy.location.client.ClientClient;
import tn.sesame.driveeasy.location.client.VehiculeClient;
import tn.sesame.driveeasy.location.dto.*;
import tn.sesame.driveeasy.location.exception.*;
import tn.sesame.driveeasy.location.model.*;
import tn.sesame.driveeasy.location.repository.LocationRepository;

@Service
public class LocationService {

    private static final Logger log = LoggerFactory.getLogger(LocationService.class);

    private final LocationRepository repo;
    private final VehiculeClient vehiculeClient;
    private final ClientClient clientClient;

    // business properties, all from location-service.yml (Config Server)
    private final BigDecimal penaliteCoef, remiseFidele, remiseVip, coefEco, coefBerline, coefSuv;
    private final int seuilFidele, seuilVip;

    public LocationService(LocationRepository repo, VehiculeClient vehiculeClient, ClientClient clientClient,
                           @Value("${driveeasy.location.penalite-coefficient:1.5}") BigDecimal penaliteCoef,
                           @Value("${driveeasy.location.remise-fidele:0.05}") BigDecimal remiseFidele,
                           @Value("${driveeasy.location.remise-vip:0.10}") BigDecimal remiseVip,
                           @Value("${driveeasy.location.coefficient-economique:1.0}") BigDecimal coefEco,
                           @Value("${driveeasy.location.coefficient-berline:1.2}") BigDecimal coefBerline,
                           @Value("${driveeasy.location.coefficient-suv:1.5}") BigDecimal coefSuv,
                           @Value("${driveeasy.location.seuil-fidele:5}") int seuilFidele,
                           @Value("${driveeasy.location.seuil-vip:15}") int seuilVip) {
        this.repo = repo;
        this.vehiculeClient = vehiculeClient;
        this.clientClient = clientClient;
        this.penaliteCoef = penaliteCoef;
        this.remiseFidele = remiseFidele;
        this.remiseVip = remiseVip;
        this.coefEco = coefEco;
        this.coefBerline = coefBerline;
        this.coefSuv = coefSuv;
        this.seuilFidele = seuilFidele;
        this.seuilVip = seuilVip;
    }

    // ------------------------------------------------------------------ queries

    public PageResponse<Location> search(StatutLocation statut, Long clientId, Long vehiculeId,
                                         LocalDate from, LocalDate to, int page, int size) {
        int s = Math.min(Math.max(size, 1), 100);
        int p = Math.max(page, 0);
        List<Location> filtered = repo.findAll().stream()
                .filter(l -> statut == null || l.getStatut() == statut)
                .filter(l -> clientId == null || l.getClientId().equals(clientId))
                .filter(l -> vehiculeId == null || l.getVehiculeId().equals(vehiculeId))
                .filter(l -> from == null || !l.getDateFinPrevue().isBefore(from))
                .filter(l -> to == null || !l.getDateDebut().isAfter(to))
                .toList();
        int total = filtered.size();
        int fromIdx = Math.min(p * s, total);
        int toIdx = Math.min(fromIdx + s, total);
        int totalPages = (int) Math.ceil(total / (double) s);
        return new PageResponse<>(filtered.subList(fromIdx, toIdx), p, s, total, totalPages);
    }

    /** Rental enriched with client and vehicle data (aggregation of 3 services). */
    public LocationDetail detail(Long id) {
        Location l = find(id);
        ClientDto client = null;
        VehiculeDto vehicule = null;
        try { client = clientClient.getClient(l.getClientId()); }
        catch (FeignException e) { log.warn("client {} not retrievable: {}", l.getClientId(), e.status()); }
        try { vehicule = vehiculeClient.getVehicule(l.getVehiculeId()); }
        catch (FeignException e) { log.warn("vehicule {} not retrievable: {}", l.getVehiculeId(), e.status()); }
        return new LocationDetail(l, client, vehicule);
    }

    public StatistiquesResponse statistiques() {
        List<VehiculeDto> vehicules = vehiculeClient.list();
        Map<Long, VehiculeDto> byId = vehicules.stream().collect(Collectors.toMap(VehiculeDto::id, v -> v));
        List<Location> all = repo.findAll();

        BigDecimal ca = all.stream().filter(l -> l.getStatut() == StatutLocation.TERMINEE)
                .map(Location::getMontantTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        YearMonth now = YearMonth.now();
        BigDecimal caMois = all.stream()
                .filter(l -> l.getStatut() == StatutLocation.TERMINEE && l.getDateRetourEffectif() != null
                        && YearMonth.from(l.getDateRetourEffectif()).equals(now))
                .map(Location::getMontantTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        long enCours = all.stream().filter(l -> l.getStatut() == StatutLocation.EN_COURS).count();
        long terminees = all.stream().filter(l -> l.getStatut() == StatutLocation.TERMINEE).count();

        long loues = vehicules.stream().filter(v -> "LOUE".equals(v.statut())).count();
        double taux = vehicules.isEmpty() ? 0 : Math.round(1000.0 * loues / vehicules.size()) / 10.0;

        Map<Long, Long> counts = all.stream().filter(l -> l.getStatut() != StatutLocation.ANNULEE)
                .collect(Collectors.groupingBy(Location::getVehiculeId, Collectors.counting()));
        List<StatistiquesResponse.TopVehicule> top = counts.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    VehiculeDto v = byId.get(e.getKey());
                    String name = v == null ? "Vehicule " + e.getKey() : v.marque() + " " + v.modele();
                    return new StatistiquesResponse.TopVehicule(e.getKey(), name, e.getValue());
                }).toList();

        return new StatistiquesResponse(ca, caMois, enCours, terminees, taux, top);
    }

    // ------------------------------------------------------------------ pricing

    public SimulationResponse simulate(LocationRequest r) {
        validateDates(r.dateDebut(), r.dateFinPrevue());
        return computePrice(fetchClient(r.clientId()), fetchVehicule(r.vehiculeId()),
                r.dateDebut(), r.dateFinPrevue());
    }

    /** price = days x daily price x category coefficient x (1 - loyalty discount) */
    private SimulationResponse computePrice(ClientDto client, VehiculeDto vehicule, LocalDate debut, LocalDate fin) {
        long jours = Math.max(1, ChronoUnit.DAYS.between(debut, fin));
        BigDecimal coef = coefficient(vehicule.categorie());
        BigDecimal remise = remise(client.categorieClient());
        BigDecimal total = vehicule.prixJournalier()
                .multiply(BigDecimal.valueOf(jours))
                .multiply(coef)
                .multiply(BigDecimal.ONE.subtract(remise))
                .setScale(2, RoundingMode.HALF_UP);
        return new SimulationResponse(jours, vehicule.prixJournalier(), coef, remise, total);
    }

    private BigDecimal coefficient(String categorie) {
        if (categorie == null) return coefEco;
        return switch (categorie.toUpperCase()) {
            case "BERLINE" -> coefBerline;
            case "SUV" -> coefSuv;
            default -> coefEco;
        };
    }

    private BigDecimal remise(String categorieClient) {
        if (categorieClient == null) return BigDecimal.ZERO;
        return switch (categorieClient.toUpperCase()) {
            case "FIDELE" -> remiseFidele;
            case "VIP" -> remiseVip;
            default -> BigDecimal.ZERO;
        };
    }

    // ------------------------------------------------------------------ lifecycle

    /** synchronized: the availability check and the save must not interleave between two requests */
    public synchronized Location create(LocationRequest r) {
        validateDates(r.dateDebut(), r.dateFinPrevue());

        ClientDto client = fetchClient(r.clientId());
        EligibiliteDto elig = fetchEligibilite(r.clientId());
        if (!elig.eligible()) throw new ConflictException("Client not eligible: " + elig.raison());

        VehiculeDto vehicule = fetchVehicule(r.vehiculeId());
        if ("MAINTENANCE".equals(vehicule.statut()) || "LOUE".equals(vehicule.statut()))
            throw new ConflictException("Vehicle cannot be booked (status " + vehicule.statut() + ")");
        if (overlaps(r.vehiculeId(), r.dateDebut(), r.dateFinPrevue()))
            throw new ConflictException("Vehicle already booked for this period");

        SimulationResponse price = computePrice(client, vehicule, r.dateDebut(), r.dateFinPrevue());

        Location l = new Location();
        l.setClientId(r.clientId());
        l.setVehiculeId(r.vehiculeId());
        l.setDateDebut(r.dateDebut());
        l.setDateFinPrevue(r.dateFinPrevue());
        l.setPrixJournalierApplique(vehicule.prixJournalier());   // price frozen at booking time
        l.setMontantTotal(price.montantTotal());
        l.setStatut(StatutLocation.RESERVEE);
        l.setDateCreation(LocalDateTime.now());
        return repo.save(l);
    }

    public Location depart(Long id, Integer kmDepart) {
        Location l = find(id);
        if (l.getStatut() != StatutLocation.RESERVEE)
            throw new ConflictException("Only a RESERVEE rental can start (current: " + l.getStatut() + ")");

        VehiculeDto v = fetchVehicule(l.getVehiculeId());
        if (!"DISPONIBLE".equals(v.statut()))
            throw new ConflictException("Vehicle is not available (status " + v.statut() + ")");

        setVehiculeStatut(l.getVehiculeId(), "LOUE");
        try {
            l.setKmDepart(kmDepart != null ? kmDepart : v.kilometrage());
            l.setStatut(StatutLocation.EN_COURS);
            return repo.save(l);
        } catch (RuntimeException e) {
            // compensation: give the vehicle back
            l.setStatut(StatutLocation.RESERVEE);
            setVehiculeStatut(l.getVehiculeId(), "DISPONIBLE");
            throw e;
        }
    }

    public Location retour(Long id, RetourRequest r) {
        Location l = find(id);
        if (l.getStatut() != StatutLocation.EN_COURS)
            throw new ConflictException("Only an EN_COURS rental can be returned (current: " + l.getStatut() + ")");
        if (r.kmRetour() < l.getKmDepart())
            throw new IllegalArgumentException("kmRetour cannot be lower than kmDepart (" + l.getKmDepart() + ")");

        LocalDate retour = r.dateRetour() != null ? r.dateRetour() : LocalDate.now();
        if (retour.isBefore(l.getDateDebut()))
            throw new IllegalArgumentException("dateRetour cannot be before dateDebut");

        // late penalty: coefficient x daily price x days late
        long retard = Math.max(0, ChronoUnit.DAYS.between(l.getDateFinPrevue(), retour));
        BigDecimal penalite = l.getPrixJournalierApplique()
                .multiply(penaliteCoef)
                .multiply(BigDecimal.valueOf(retard))
                .setScale(2, RoundingMode.HALF_UP);

        setVehiculeStatut(l.getVehiculeId(), "DISPONIBLE");
        BigDecimal ancienMontant = l.getMontantTotal();
        try {
            l.setDateRetourEffectif(retour);
            l.setKmRetour(r.kmRetour());
            l.setPenalite(penalite);
            l.setMontantTotal(ancienMontant.add(penalite));
            l.setStatut(StatutLocation.TERMINEE);
            repo.save(l);
        } catch (RuntimeException e) {
            // compensation: vehicle goes back to LOUE, rental stays EN_COURS
            l.setStatut(StatutLocation.EN_COURS);
            l.setMontantTotal(ancienMontant);
            l.setPenalite(BigDecimal.ZERO);
            setVehiculeStatut(l.getVehiculeId(), "LOUE");
            throw e;
        }
        updateLoyalty(l.getClientId());
        return l;
    }

    public Location annuler(Long id) {
        Location l = find(id);
        if (l.getStatut() != StatutLocation.RESERVEE)
            throw new ConflictException("Only a RESERVEE rental can be cancelled (current: " + l.getStatut() + ")");
        l.setStatut(StatutLocation.ANNULEE);
        return repo.save(l);
    }

    /** FIDELE after seuil-fidele finished rentals, VIP after seuil-vip. Upgrade only, never downgrade. */
    private void updateLoyalty(Long clientId) {
        long done = repo.findAll().stream()
                .filter(x -> x.getClientId().equals(clientId) && x.getStatut() == StatutLocation.TERMINEE)
                .count();
        String target = done >= seuilVip ? "VIP" : done >= seuilFidele ? "FIDELE" : null;
        if (target == null) return;
        try {
            ClientDto c = clientClient.getClient(clientId);
            if (rank(target) > rank(c.categorieClient())) {
                clientClient.changeCategorie(clientId, new CategorieRequest(target));
                log.info("client {} upgraded to {}", clientId, target);
            }
        } catch (FeignException e) {
            // the return itself already succeeded: the upgrade is retried at the next return
            log.warn("loyalty upgrade failed for client {}: {}", clientId, e.status());
        }
    }

    private int rank(String categorie) {
        if ("VIP".equals(categorie)) return 2;
        if ("FIDELE".equals(categorie)) return 1;
        return 0;
    }

    // ------------------------------------------------------------------ helpers

    private Location find(Long id) {
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Location " + id + " not found"));
    }

    private void validateDates(LocalDate debut, LocalDate fin) {
        if (fin.isBefore(debut)) throw new IllegalArgumentException("dateFinPrevue must not be before dateDebut");
        if (debut.isBefore(LocalDate.now())) throw new IllegalArgumentException("dateDebut cannot be in the past");
    }

    private boolean overlaps(Long vehiculeId, LocalDate debut, LocalDate fin) {
        return repo.findAll().stream()
                .filter(l -> l.getVehiculeId().equals(vehiculeId))
                .filter(l -> l.getStatut() == StatutLocation.RESERVEE || l.getStatut() == StatutLocation.EN_COURS)
                .anyMatch(l -> !l.getDateDebut().isAfter(fin) && !debut.isAfter(l.getDateFinPrevue()));
    }

    private ClientDto fetchClient(Long id) {
        try { return clientClient.getClient(id); }
        catch (FeignException.NotFound e) { throw new NotFoundException("Client " + id + " not found"); }
    }

    private EligibiliteDto fetchEligibilite(Long id) {
        try { return clientClient.eligibilite(id); }
        catch (FeignException.NotFound e) { throw new NotFoundException("Client " + id + " not found"); }
    }

    private VehiculeDto fetchVehicule(Long id) {
        try { return vehiculeClient.getVehicule(id); }
        catch (FeignException.NotFound e) { throw new NotFoundException("Vehicule " + id + " not found"); }
    }

    private void setVehiculeStatut(Long id, String statut) {
        try { vehiculeClient.changeStatut(id, new StatutRequest(statut)); }
        catch (FeignException.NotFound e) { throw new NotFoundException("Vehicule " + id + " not found"); }
    }
}