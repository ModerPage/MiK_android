package me.modernpage.data.remote.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class AccountInfo {
    private final String username;
    private final String email;
    private final String fullname;
    private final Date birthdate;

    @Expose(deserialize = false)
    private final String password;

    @SerializedName(value = "avatarUrl")
    @Expose(serialize = false)
    private final String avatar;

    public AccountInfo(String username,
                       String email,
                       String fullname,
                       Date birthdate,
                       String password,
                       String avatar) {
        this.username = username;
        this.email = email;
        this.fullname = fullname;
        this.birthdate = birthdate;
        this.password = password;
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullname() {
        return fullname;
    }

    public Date getBirthdate() {
        return birthdate;
    }

    public String getAvatar() {
        return avatar;
    }
}
