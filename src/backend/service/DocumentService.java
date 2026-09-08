package backend.service;

import backend.enums.DocumentType;
import backend.model.Document;
import backend.storage.DocumentStorage;

import java.util.List;
import java.util.UUID;

public class DocumentService {

    private final DocumentStorage documentStorage;
    private final TenderBidService tenderBidService;


    public DocumentService(
            DocumentStorage documentStorage,
            TenderBidService tenderBidService
    ) {
        this.documentStorage = documentStorage;
        this.tenderBidService = tenderBidService;
    }


    public Document createDocument(
            UUID tenderBidId,
            DocumentType documentType,
            String fileName
    ) {

        // Check if TenderBid exists
        tenderBidService.getTenderBidById(
                tenderBidId
        );

        Document document = new Document(
                tenderBidId,
                documentType,
                fileName
        );

        documentStorage.saveDocument(
                document
        );

        return document;
    }


    public Document getDocumentById(
            UUID id
    ) {

        Document document =
                documentStorage
                        .findDocumentById(id);

        if (document == null) {

            throw new IllegalArgumentException(
                    "Document not found"
            );
        }

        return document;
    }


    public List<Document> getAllDocuments() {

        return documentStorage
                .getAllDocuments();
    }


    public List<Document> getDocumentsByTenderBidId(
            UUID tenderBidId
    ) {

        // Check if TenderBid exists
        tenderBidService.getTenderBidById(
                tenderBidId
        );

        return documentStorage
                .getDocumentsByTenderBidId(
                        tenderBidId
                );
    }


    public void startDocumentProcessing(
            UUID id
    ) {

        Document document =
                getDocumentById(id);

        document.startProcessing();
    }


    public void markDocumentAsProcessed(
            UUID id
    ) {

        Document document =
                getDocumentById(id);

        document.markAsProcessed();
    }


    public void markDocumentAsFailed(
            UUID id
    ) {

        Document document =
                getDocumentById(id);

        document.markAsFailed();
    }


    public void deleteDocument(
            UUID id
    ) {

        // Check if document exists
        getDocumentById(id);

        documentStorage.deleteDocument(id);
    }
}