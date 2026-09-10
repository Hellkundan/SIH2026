package backend.service;

import backend.model.Tender;
import backend.repository.TenderRepository;

import java.util.List;
import java.util.UUID;

public class TenderService {

    private final TenderRepository tenderRepository;


    public TenderService(TenderRepository tenderRepository) {
        this.tenderRepository = tenderRepository;
    }


    public Tender createTender(
            String title,
            String description
    ) {

        Tender tender = new Tender(
                title,
                description
        );

        return tenderRepository.save(tender);
    }


    public Tender getTenderById(UUID id) {

        return tenderRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tender not found"
                ));
    }


    public List<Tender> getAllTenders() {

        return tenderRepository.findAll();
    }


    public void updateTender(
            UUID id,
            String title,
            String description
    ) {

        Tender tender = getTenderById(id);

        tender.updateTender(
                title,
                description
        );

        tenderRepository.save(tender);
    }


    public void openTender(UUID id) {

        Tender tender = getTenderById(id);

        tender.openTender();

        tenderRepository.save(tender);
    }


    public void closeTender(UUID id) {

        Tender tender = getTenderById(id);

        tender.closeTender();

        tenderRepository.save(tender);
    }


    public void deleteTender(UUID id) {

        getTenderById(id);

        tenderRepository.deleteById(id);
    }
}