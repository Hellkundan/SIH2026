package backend.dto.response;

public class VerificationResultResponse {
    private String type;
    private String status;
    private String provider;
    private String errorState;
    private String checkedAt;
    private String message;

    public VerificationResultResponse() {
    }

    public VerificationResultResponse(String type, String status, String provider, String errorState, String checkedAt, String message) {
        this.type = type;
        this.status = status;
        this.provider = provider;
        this.errorState = errorState;
        this.checkedAt = checkedAt;
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getErrorState() {
        return errorState;
    }

    public void setErrorState(String errorState) {
        this.errorState = errorState;
    }

    public String getCheckedAt() {
        return checkedAt;
    }

    public void setCheckedAt(String checkedAt) {
        this.checkedAt = checkedAt;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
