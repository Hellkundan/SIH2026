package backend.storage;

import backend.model.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DocumentStorage {

    private final HashMap<UUID, Document> documents =
            new HashMap<>();


    public void saveDocument(Document document) {

        documents.put(
                document.getId(),
                document
        );
    }


    public Document findDocumentById(UUID id) {

        return documents.get(id);
    }


    public List<Document> getAllDocuments() {

        return new ArrayList<>(
                documents.values()
        );
    }


    public List<Document> getDocumentsByTenderBidId(
            UUID tenderBidId
    ) {

        List<Document> result =
                new ArrayList<>();

        for (Document document :
                documents.values()) {

            if (document.getTenderBidId()
                    .equals(tenderBidId)) {

                result.add(document);
            }
        }

        return result;
    }


    public void deleteDocument(UUID id) {

        documents.remove(id);
    }
}