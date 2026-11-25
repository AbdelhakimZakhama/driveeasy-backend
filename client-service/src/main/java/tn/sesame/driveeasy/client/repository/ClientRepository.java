package tn.sesame.driveeasy.client.repository;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import tn.sesame.driveeasy.client.model.*;

@Repository
public class ClientRepository {

    private final Map<Long, Client> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public List<Client> findAll() {
        return store.values().stream().sorted(Comparator.comparing(Client::getId)).toList();
    }

    public Optional<Client> findById(Long id) { return Optional.ofNullable(store.get(id)); }

    public boolean existsByCin(String cin, Long excludeId) {
        return store.values().stream().anyMatch(c -> c.getCin().equalsIgnoreCase(cin) && !Objects.equals(c.getId(), excludeId));
    }

    public boolean existsByEmail(String email, Long excludeId) {
        return store.values().stream().anyMatch(c -> c.getEmail().equalsIgnoreCase(email) && !Objects.equals(c.getId(), excludeId));
    }

    public boolean existsByNumeroPermis(String permis, Long excludeId) {
        return store.values().stream().anyMatch(c -> c.getNumeroPermis().equalsIgnoreCase(permis) && !Objects.equals(c.getId(), excludeId));
    }

    public Client save(Client c) {
        if (c.getId() == null) c.setId(sequence.incrementAndGet());
        store.put(c.getId(), c);
        return c;
    }

    public void deleteById(Long id) { store.remove(id); }

    @PostConstruct
    void seed() {
        add("08123456", "Ben Ali", "Sami", "1995-04-12", "sami.benali@mail.tn", "+21620111222", "Tunis", "P-1001", "2029-06-30", CategorieClient.STANDARD);
        add("09234567", "Trabelsi", "Amira", "1990-09-03", "amira.trabelsi@mail.tn", "+21621222333", "Sfax", "P-1002", "2027-01-15", CategorieClient.FIDELE);
        add("07345678", "Gharbi", "Youssef", "1985-12-20", "youssef.gharbi@mail.tn", "+21622333444", "Sousse", "P-1003", "2030-03-01", CategorieClient.VIP);
        add("10456789", "Jlassi", "Nour", "2008-05-10", "nour.jlassi@mail.tn", "+21623444555", "Nabeul", "P-1004", "2031-01-01", CategorieClient.STANDARD); // too young
        add("11567890", "Mansouri", "Karim", "1992-02-28", "karim.mansouri@mail.tn", "+21624555666", "Bizerte", "P-1005", "2024-05-01", CategorieClient.STANDARD); // expired licence
    }

    private void add(String cin, String nom, String prenom, String naiss, String email, String tel,
                     String adr, String permis, String expiration, CategorieClient cat) {
        Client c = new Client();
        c.setCin(cin); c.setNom(nom); c.setPrenom(prenom);
        c.setDateNaissance(LocalDate.parse(naiss)); c.setEmail(email);
        c.setTelephone(tel); c.setAdresse(adr); c.setNumeroPermis(permis);
        c.setDateExpirationPermis(LocalDate.parse(expiration));
        c.setCategorieClient(cat); c.setDateInscription(LocalDate.now());
        save(c);
    }
}