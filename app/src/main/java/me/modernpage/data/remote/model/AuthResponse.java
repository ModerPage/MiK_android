package me.modernpage.data.remote.model;

import java.io.Serializable;

import me.modernpage.data.local.entity.Profile;

public class AuthResponse implements Serializable {
    private Profile profile;
    private String token;

    public AuthResponse() {
    }

    public AuthResponse(Profile profile, String token) {
        this.profile = profile;
        this.token = token;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "profile=" + profile +
                ", token='" + token + '\'' +
                '}';
    }
}
