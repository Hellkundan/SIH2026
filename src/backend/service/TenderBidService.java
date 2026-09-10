package backend.service;

import backend.model.TenderBid;
import backend.repository.TenderBidRepository;

import java.util.List;
import java.util.UUID;

public class TenderBidService {

    private final TenderBidRepository tenderBidRepository;
    private final TenderService tenderService;
    private final BidderService bidderService;


    public TenderBidService(
            TenderBidRepository tenderBidRepository,
            TenderService tenderService,
            BidderService bidderService
    ) {
        this.tenderBidRepository = tenderBidRepository;
        this.tenderService = tenderService;
        this.bidderService = bidderService;
    }


    public TenderBid createTenderBid(
            UUID tenderId,
            UUID bidderId
    ) {

        tenderService.getTenderById(tenderId);

        bidderService.getBidderById(bidderId);

        TenderBid tenderBid = new TenderBid(
                tenderId,
                bidderId
        );

        return tenderBidRepository.save(tenderBid);
    }


    public TenderBid getTenderBidById(UUID id) {

        return tenderBidRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tender bid not found"
                ));
    }


    public List<TenderBid> getAllTenderBids() {

        return tenderBidRepository.findAll();
    }


    public List<TenderBid> getTenderBidsByTenderId(UUID tenderId) {

        tenderService.getTenderById(tenderId);

        return tenderBidRepository.findByTenderId(tenderId);
    }


    public List<TenderBid> getTenderBidsByBidderId(UUID bidderId) {

        bidderService.getBidderById(bidderId);

        return tenderBidRepository.findByBidderId(bidderId);
    }


    public void submitTenderBid(UUID id) {

        TenderBid tenderBid = getTenderBidById(id);

        tenderBid.submitBid();

        tenderBidRepository.save(tenderBid);
    }


    public void startBidReview(UUID id) {

        TenderBid tenderBid = getTenderBidById(id);

        tenderBid.startReview();

        tenderBidRepository.save(tenderBid);
    }


    public void qualifyBid(UUID id) {

        TenderBid tenderBid = getTenderBidById(id);

        tenderBid.qualifyBid();

        tenderBidRepository.save(tenderBid);
    }


    public void disqualifyBid(UUID id) {

        TenderBid tenderBid = getTenderBidById(id);

        tenderBid.disqualifyBid();

        tenderBidRepository.save(tenderBid);
    }


    public void deleteTenderBid(UUID id) {

        getTenderBidById(id);

        tenderBidRepository.deleteById(id);
    }
}