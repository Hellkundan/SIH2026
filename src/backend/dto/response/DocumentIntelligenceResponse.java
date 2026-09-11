package backend.dto.response;

public class DocumentIntelligenceResponse {
    private String documentId;
    private OcrResultResponse ocr;
    private VerificationResultResponse verification;

    public DocumentIntelligenceResponse() {
    }

    public DocumentIntelligenceResponse(String documentId, OcrResultResponse ocr, VerificationResultResponse verification) {
        this.documentId = documentId;
        this.ocr = ocr;
        this.verification = verification;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public OcrResultResponse getOcr() {
        return ocr;
    }

    public void setOcr(OcrResultResponse ocr) {
        this.ocr = ocr;
    }

    public VerificationResultResponse getVerification() {
        return verification;
    }

    public void setVerification(VerificationResultResponse verification) {
        this.verification = verification;
    }
}
