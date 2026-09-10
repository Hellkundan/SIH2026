package backend.repository;

import backend.model.CartelSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CartelSignalRepository extends JpaRepository<CartelSignal, UUID> {
    List<CartelSignal> findByTenderId(UUID tenderId);
    void deleteByTenderId(UUID tenderId);
}
