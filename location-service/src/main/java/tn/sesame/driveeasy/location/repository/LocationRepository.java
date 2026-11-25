package tn.sesame.driveeasy.location.repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import tn.sesame.driveeasy.location.model.Location;

@Repository
public class LocationRepository {

    private final Map<Long, Location> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    public List<Location> findAll() {
        return store.values().stream().sorted(Comparator.comparing(Location::getId)).toList();
    }

    public Optional<Location> findById(Long id) { return Optional.ofNullable(store.get(id)); }

    public Location save(Location l) {
        if (l.getId() == null) l.setId(sequence.incrementAndGet());
        store.put(l.getId(), l);
        return l;
    }

    public void deleteById(Long id) { store.remove(id); }
}