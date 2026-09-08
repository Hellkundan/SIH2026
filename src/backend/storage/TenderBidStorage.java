package backend.storage;

import backend.model.TenderBid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TenderBidStorage {

    private final HashMap<UUID, TenderBid> tenderBids =
            new HashMap<>();


    public void saveTenderBid(TenderBid tenderBid) {

        tenderBids.put(
                tenderBid.getId(),
                tenderBid
        );
    }


    public TenderBid findTenderBidById(UUID id) {

        return tenderBids.get(id);
    }


    public List<TenderBid> getAllTenderBids() {

        return new ArrayList<>(
                tenderBids.values()
        );
    }


    public List<TenderBid> getTenderBidsByTenderId(
            UUID tenderId
    ) {

        List<TenderBid> result =
                new ArrayList<>();

        for (TenderBid tenderBid :
                tenderBids.values()) {

            if (tenderBid.getTenderId()
                    .equals(tenderId)) {

                result.add(tenderBid);
            }
        }

        return result;
    }


    public List<TenderBid> getTenderBidsByBidderId(
            UUID bidderId
    ) {

        List<TenderBid> result =
                new ArrayList<>();

        for (TenderBid tenderBid :
                tenderBids.values()) {

            if (tenderBid.getBidderId()
                    .equals(bidderId)) {

                result.add(tenderBid);
            }
        }

        return result;
    }


    public void deleteTenderBid(UUID id) {

        tenderBids.remove(id);
    }
}