package backend.repository;

import backend.model.TenderRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TenderRequirementRepository extends JpaRepository<TenderRequirement, UUID> {
    List<TenderRequirement> findByTenderId(UUID tenderId);
}
