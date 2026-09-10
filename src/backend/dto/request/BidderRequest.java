package backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class BidderRequest {

    @NotBlank
    private String companyName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String phone;

    private String pan;

    private String gstin;


    public BidderRequest() {
    }


    public BidderRequest(
            String companyName,
            String email,
            String phone,
            String pan,
            String gstin
    ) {

        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
        this.pan = pan;
        this.gstin = gstin;
    }


    public BidderRequest(String companyName, String email, String phone) {
        this(companyName, email, phone, null, null);
    }


    public String getCompanyName() {

        return companyName;
    }


    public String getEmail() {

        return email;
    }


    public String getPhone() {

        return phone;
    }


    public String getPan() {
        return pan;
    }


    public String getGstin() {
        return gstin;
    }


    public void setCompanyName(String companyName) {

        this.companyName = companyName;
    }


    public void setEmail(String email) {

        this.email = email;
    }


    public void setPhone(String phone) {

        this.phone = phone;
    }


    public void setPan(String pan) {
        this.pan = pan;
    }


    public void setGstin(String gstin) {
        this.gstin = gstin;
    }
}