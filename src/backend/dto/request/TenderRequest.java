package backend.dto.request;

import jakarta.validation.constraints.NotBlank;

public class TenderRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String description;


    public TenderRequest() {
    }


    public TenderRequest(String title, String description) {

        this.title = title;
        this.description = description;
    }


    public String getTitle() {

        return title;
    }


    public String getDescription() {

        return description;
    }


    public void setTitle(String title) {

        this.title = title;
    }


    public void setDescription(String description) {

        this.description = description;
    }
}