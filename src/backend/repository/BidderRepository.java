package backend.repository;

import backend.model.Bidder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BidderRepository extends JpaRepository<Bidder, UUID> {
}
