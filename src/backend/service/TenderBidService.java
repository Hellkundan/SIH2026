package backend.service;

import backend.model.TenderBid;
import backend.storage.TenderBidStorage;

import java.util.List;
import java.util.UUID;

public class TenderBidService {

    private final TenderBidStorage tenderBidStorage;
    private final TenderService tenderService;
    private final BidderService bidderService;


    public TenderBidService(
            TenderBidStorage tenderBidStorage,
            TenderService tenderService,
            BidderService bidderService
    ) {
        this.tenderBidStorage = tenderBidStorage;
        this.tenderService = tenderService;
        this.bidderService = bidderService;
    }


    public TenderBid createTenderBid(
            UUID tenderId,
            UUID bidderId
    ) {

        // Check if Tender exists
        tenderService.getTenderById(tenderId);

        // Check if Bidder exists
        bidderService.getBidderById(bidderId);

        TenderBid tenderBid = new TenderBid(
                tenderId,
                bidderId
        );

        tenderBidStorage.saveTenderBid(
                tenderBid
        );

        return tenderBid;
    }


    public TenderBid getTenderBidById(
            UUID id
    ) {

        TenderBid tenderBid =
                tenderBidStorage
                        .findTenderBidById(id);

        if (tenderBid == null) {

            throw new IllegalArgumentException(
                    "Tender bid not found"
            );
        }

        return tenderBid;
    }


    public List<TenderBid> getAllTenderBids() {

        return tenderBidStorage
                .getAllTenderBids();
    }


    public List<TenderBid> getTenderBidsByTenderId(
            UUID tenderId
    ) {

        // Check if Tender exists
        tenderService.getTenderById(tenderId);

        return tenderBidStorage
                .getTenderBidsByTenderId(tenderId);
    }


    public List<TenderBid> getTenderBidsByBidderId(
            UUID bidderId
    ) {

        // Check if Bidder exists
        bidderService.getBidderById(bidderId);

        return tenderBidStorage
                .getTenderBidsByBidderId(bidderId);
    }


    public void submitTenderBid(
            UUID id
    ) {

        TenderBid tenderBid =
                getTenderBidById(id);

        tenderBid.submitBid();
    }


    public void startBidReview(
            UUID id
    ) {

        TenderBid tenderBid =
                getTenderBidById(id);

        tenderBid.startReview();
    }


    public void qualifyBid(
            UUID id
    ) {

        TenderBid tenderBid =
                getTenderBidById(id);

        tenderBid.qualifyBid();
    }


    public void disqualifyBid(
            UUID id
    ) {

        TenderBid tenderBid =
                getTenderBidById(id);

        tenderBid.disqualifyBid();
    }


    public void deleteTenderBid(
            UUID id
    ) {

        // Check if the TenderBid exists
        getTenderBidById(id);

        tenderBidStorage.deleteTenderBid(id);
    }
}