package backend.service;

import backend.model.Tender;
import backend.storage.TenderStorage;

import java.util.List;
import java.util.UUID;

public class TenderService {

    private final TenderStorage tenderStorage;


    public TenderService(TenderStorage tenderStorage) {
        this.tenderStorage = tenderStorage;
    }


    public Tender createTender(
            String title,
            String description
    ) {

        Tender tender = new Tender(
                title,
                description
        );

        tenderStorage.saveTender(tender);

        return tender;
    }


    public Tender getTenderById(UUID id) {

        Tender tender =
                tenderStorage.findTenderById(id);

        if (tender == null) {

            throw new IllegalArgumentException(
                    "Tender not found"
            );
        }

        return tender;
    }


    public List<Tender> getAllTenders() {

        return tenderStorage.getAllTenders();
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
    }


    public void openTender(UUID id) {

        Tender tender = getTenderById(id);

        tender.openTender();
    }


    public void closeTender(UUID id) {

        Tender tender = getTenderById(id);

        tender.closeTender();
    }


    public void deleteTender(UUID id) {

        getTenderById(id);

        tenderStorage.deleteTender(id);
    }
}