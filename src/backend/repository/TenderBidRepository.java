package backend.repository;

import backend.model.TenderBid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TenderBidRepository extends JpaRepository<TenderBid, UUID> {
    List<TenderBid> findByTenderId(UUID tenderId);
    List<TenderBid> findByBidderId(UUID bidderId);
}
