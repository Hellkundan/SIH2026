package backend.repository;

import backend.model.Tender;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TenderRepository extends JpaRepository<Tender, UUID> {
}
