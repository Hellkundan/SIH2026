package backend.storage;

import backend.model.Tender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TenderStorage {

    private final HashMap<UUID, Tender> tenders = new HashMap<>();


    public void saveTender(Tender tender) {

        tenders.put(
                tender.getId(),
                tender
        );
    }


    public Tender findTenderById(UUID id) {

        return tenders.get(id);
    }


    public List<Tender> getAllTenders() {

        return new ArrayList<>(
                tenders.values()
        );
    }


    public void deleteTender(UUID id) {

        tenders.remove(id);
    }
}