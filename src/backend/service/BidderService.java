package backend.service;

import backend.model.Bidder;
import backend.repository.BidderRepository;

import java.util.List;
import java.util.UUID;

public class BidderService {

    private final BidderRepository bidderRepository;


    public BidderService(BidderRepository bidderRepository) {
        this.bidderRepository = bidderRepository;
    }


    public Bidder createBidder(
            String companyName,
            String email,
            String phone,
            String pan,
            String gstin
    ) {

        Bidder bidder = new Bidder(
                companyName,
                email,
                phone
        );
            bidder.setIdentifiers(pan, gstin);

        return bidderRepository.save(bidder);
    }


    public Bidder createBidder(String companyName, String email, String phone) {
        return createBidder(companyName, email, phone, null, null);
    }


    public Bidder getBidderById(UUID id) {

        return bidderRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Bidder not found"
                ));
    }


    public List<Bidder> getAllBidders() {

        return bidderRepository.findAll();
    }


    public void updateBidder(
            UUID id,
            String companyName,
            String email,
            String phone,
            String pan,
            String gstin
    ) {

        Bidder bidder = getBidderById(id);

        bidder.updateBidder(
                companyName,
                email,
                phone,
                pan,
                gstin
        );

        bidderRepository.save(bidder);
    }


    public void deleteBidder(UUID id) {

        getBidderById(id);

        bidderRepository.deleteById(id);
    }
}