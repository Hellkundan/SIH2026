package backend.dto.request;

import backend.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class DocumentRequest {

    @NotNull
    private UUID tenderBidId;

    @NotNull
    private DocumentType documentType;

    @NotBlank
    private String fileName;


    public DocumentRequest() {
    }


    public DocumentRequest(
            UUID tenderBidId,
            DocumentType documentType,
            String fileName
    ) {

        this.tenderBidId = tenderBidId;
        this.documentType = documentType;
        this.fileName = fileName;
    }


    public UUID getTenderBidId() {

        return tenderBidId;
    }


    public DocumentType getDocumentType() {

        return documentType;
    }


    public String getFileName() {

        return fileName;
    }


    public void setTenderBidId(UUID tenderBidId) {

        this.tenderBidId = tenderBidId;
    }


    public void setDocumentType(DocumentType documentType) {

        this.documentType = documentType;
    }


    public void setFileName(String fileName) {

        this.fileName = fileName;
    }
}