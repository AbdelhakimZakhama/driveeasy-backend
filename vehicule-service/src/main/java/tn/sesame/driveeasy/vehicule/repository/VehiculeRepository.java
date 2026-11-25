package tn.sesame.driveeasy.vehicule.repository;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import tn.sesame.driveeasy.vehicule.model.*;

@Repository
public class VehiculeRepository {

    private final Map<Long, Vehicule> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public List<Vehicule> findAll() {
        return store.values().stream()
                .sorted(Comparator.comparing(Vehicule::getId)).toList();
    }

    public Optional<Vehicule> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean existsByImmatriculation(String immat, Long excludeId) {
        return store.values().stream().anyMatch(v ->
                v.getImmatriculation().equalsIgnoreCase(immat)
                        && !Objects.equals(v.getId(), excludeId));
    }

    public Vehicule save(Vehicule v) {
        if (v.getId() == null) v.setId(sequence.incrementAndGet());
        store.put(v.getId(), v);
        return v;
    }

    public void deleteById(Long id) { store.remove(id); }

    // sample data loaded in RAM at startup
    @PostConstruct
    void seed() {
        add("123 TUN 4567", "Peugeot", "208", 2022, "Blanc", Carburant.ESSENCE, Boite.MANUELLE, 5, 25000, "80", "ECONOMIQUE");
        add("234 TUN 8910", "Renault", "Clio", 2021, "Gris", Carburant.DIESEL, Boite.MANUELLE, 5, 41000, "75", "ECONOMIQUE");
        add("345 TUN 1112", "Volkswagen", "Golf", 2023, "Noir", Carburant.ESSENCE, Boite.AUTOMATIQUE, 5, 12000, "120", "BERLINE");
        add("456 TUN 1314", "Toyota", "Yaris Hybrid", 2023, "Rouge", Carburant.HYBRIDE, Boite.AUTOMATIQUE, 5, 9000, "110", "ECONOMIQUE");
        add("567 TUN 1516", "Hyundai", "Tucson", 2022, "Bleu", Carburant.DIESEL, Boite.AUTOMATIQUE, 5, 30000, "180", "SUV");
        add("678 TUN 1718", "Dacia", "Duster", 2020, "Vert", Carburant.DIESEL, Boite.MANUELLE, 5, 68000, "95", "SUV");
    }

    private void add(String immat, String marque, String modele, int annee, String couleur,
                     Carburant c, Boite b, int places, int km, String prix, String cat) {
        Vehicule v = new Vehicule();
        v.setImmatriculation(immat); v.setMarque(marque); v.setModele(modele);
        v.setAnnee(annee); v.setCouleur(couleur); v.setCarburant(c); v.setBoite(b);
        v.setNbPlaces(places); v.setKilometrage(km);
        v.setPrixJournalier(new BigDecimal(prix));
        v.setStatut(Statut.DISPONIBLE); v.setCategorie(cat);
        save(v);
    }
}