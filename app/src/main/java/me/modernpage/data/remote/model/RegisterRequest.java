package me.modernpage.data.remote.model;

import java.io.Serializable;

public class RegisterRequest implements Serializable {
    private final String username;
    private final String fullname;
    private final String email;
    private final String password;


    public RegisterRequest(String username, String fullname, String email, String password) {
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getFullname() {
        return fullname;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
