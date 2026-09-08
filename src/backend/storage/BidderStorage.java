package backend.storage;

import backend.model.Bidder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BidderStorage {

    private final HashMap<UUID, Bidder> bidders =
            new HashMap<>();


    public void saveBidder(Bidder bidder) {

        bidders.put(
                bidder.getId(),
                bidder
        );
    }


    public Bidder findBidderById(UUID id) {

        return bidders.get(id);
    }


    public List<Bidder> getAllBidders() {

        return new ArrayList<>(
                bidders.values()
        );
    }


    public void deleteBidder(UUID id) {

        bidders.remove(id);
    }
}