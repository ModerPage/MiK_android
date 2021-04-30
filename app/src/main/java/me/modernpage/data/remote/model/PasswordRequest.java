package me.modernpage.data.remote.model;

public class PasswordRequest {
    private final String email;
    private final String password;

    public PasswordRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
