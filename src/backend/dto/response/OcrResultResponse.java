package backend.dto.response;

public class OcrResultResponse {
    private String documentNumber;
    private String holderName;
    private String issuedOn;
    private String validTill;
    private double confidence;
    private String quality;

    public OcrResultResponse() {
    }

    public OcrResultResponse(String documentNumber, String holderName, String issuedOn, String validTill, double confidence, String quality) {
        this.documentNumber = documentNumber;
        this.holderName = holderName;
        this.issuedOn = issuedOn;
        this.validTill = validTill;
        this.confidence = confidence;
        this.quality = quality;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    public String getIssuedOn() {
        return issuedOn;
    }

    public void setIssuedOn(String issuedOn) {
        this.issuedOn = issuedOn;
    }

    public String getValidTill() {
        return validTill;
    }

    public void setValidTill(String validTill) {
        this.validTill = validTill;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getQuality() {
        return quality;
    }

    public void setQuality(String quality) {
        this.quality = quality;
    }
}
