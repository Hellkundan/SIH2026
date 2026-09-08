package backend.dto.response;

import backend.security.Role;

public class LoginResponse {

    private final String token;
    private final String username;
    private final Role role;


    public LoginResponse(String token, String username, Role role) {

        this.token = token;
        this.username = username;
        this.role = role;
    }


    public String getToken() {

        return token;
    }


    public String getUsername() {

        return username;
    }


    public Role getRole() {

        return role;
    }
}