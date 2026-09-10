package backend.service;

import backend.enums.DocumentType;
import backend.model.Document;
import backend.repository.DocumentRepository;

import java.util.List;
import java.util.UUID;
import java.nio.file.Path;

public class DocumentService {

    private final DocumentRepository documentRepository;
    private final TenderBidService tenderBidService;


    public DocumentService(
            DocumentRepository documentRepository,
            TenderBidService tenderBidService
    ) {
        this.documentRepository = documentRepository;
        this.tenderBidService = tenderBidService;
    }


    public Document createDocument(
            UUID tenderBidId,
            DocumentType documentType,
            String fileName
    ) {

        tenderBidService.getTenderBidById(tenderBidId);

        Document document = new Document(
                tenderBidId,
                documentType,
                fileName
        );

        return documentRepository.save(document);
    }


    public Document getDocumentById(UUID id) {

        return documentRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Document not found"
                ));
    }


    public List<Document> getAllDocuments() {

        return documentRepository.findAll();
    }


    public List<Document> getDocumentsByTenderBidId(UUID tenderBidId) {

        tenderBidService.getTenderBidById(tenderBidId);

        return documentRepository.findByTenderBidId(tenderBidId);
    }


    public void startDocumentProcessing(UUID id) {

        Document document = getDocumentById(id);

        document.startProcessing();

        documentRepository.save(document);
    }


    public void markDocumentAsProcessed(UUID id) {

        Document document = getDocumentById(id);

        document.markAsProcessed();

        documentRepository.save(document);
    }


    public void markDocumentAsFailed(UUID id) {

        Document document = getDocumentById(id);

        document.markAsFailed();

        documentRepository.save(document);
    }


    public void setFilePath(UUID id, Path filePath) {
        Document document = getDocumentById(id);
        document.setFilePath(filePath.toString());
        documentRepository.save(document);
    }


    public void saveOcrResults(
            UUID id,
            String extractedText,
            String documentHash,
            Double classificationConfidence,
            String ocrExtractedFields
    ) {
        Document document = getDocumentById(id);
        document.setOcrResults(
                extractedText,
                documentHash,
                classificationConfidence,
                ocrExtractedFields
        );
        documentRepository.save(document);
    }


    public void deleteDocument(UUID id) {

        getDocumentById(id);

        documentRepository.deleteById(id);
    }
}