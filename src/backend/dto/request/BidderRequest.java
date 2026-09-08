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


    public BidderRequest() {
    }


    public BidderRequest(
            String companyName,
            String email,
            String phone
    ) {

        this.companyName = companyName;
        this.email = email;
        this.phone = phone;
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


    public void setCompanyName(String companyName) {

        this.companyName = companyName;
    }


    public void setEmail(String email) {

        this.email = email;
    }


    public void setPhone(String phone) {

        this.phone = phone;
    }
}