package backend.security;

import java.util.HashMap;
import java.util.Map;

public class UserStorage {

    private final Map<String, AppUser> users = new HashMap<>();


    public void saveUser(AppUser user) {

        users.put(user.getUsername(), user);
    }


    public AppUser findByUsername(String username) {

        return users.get(username);
    }
}