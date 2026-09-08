package backend.service;

import backend.model.Bidder;
import backend.storage.BidderStorage;

import java.util.List;
import java.util.UUID;

public class BidderService {

    private final BidderStorage bidderStorage;


    public BidderService(BidderStorage bidderStorage) {
        this.bidderStorage = bidderStorage;
    }


    public Bidder createBidder(
            String companyName,
            String email,
            String phone
    ) {

        Bidder bidder = new Bidder(
                companyName,
                email,
                phone
        );

        bidderStorage.saveBidder(bidder);

        return bidder;
    }


    public Bidder getBidderById(UUID id) {

        Bidder bidder =
                bidderStorage.findBidderById(id);

        if (bidder == null) {

            throw new IllegalArgumentException(
                    "Bidder not found"
            );
        }

        return bidder;
    }


    public List<Bidder> getAllBidders() {

        return bidderStorage.getAllBidders();
    }


    public void updateBidder(
            UUID id,
            String companyName,
            String email,
            String phone
    ) {

        Bidder bidder = getBidderById(id);

        bidder.updateBidder(
                companyName,
                email,
                phone
        );
    }


    public void deleteBidder(UUID id) {

        // First verify that the bidder exists
        getBidderById(id);

        bidderStorage.deleteBidder(id);
    }
}