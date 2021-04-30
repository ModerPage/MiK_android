package me.modernpage.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Entity(tableName = "AUTH_RESULT", indices = {
        @Index("TOKEN"),
        @Index(value = "USERNAME", unique = true),
        @Index("PROFILE_ID")})
public class AuthResult implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "TOKEN")
    @NonNull
    private final String token;

    @ColumnInfo(name = "USERNAME")
    @NonNull
    private final String username;

    @ColumnInfo(name = "PROFILE_ID")
    private final long profileId;


    public AuthResult(@NonNull String token, @NonNull String username, long profileId) {
        this.token = token;
        this.username = username;
        this.profileId = profileId;
    }

    @NotNull
    public String getUsername() {
        return username;
    }


    public long getProfileId() {
        return profileId;
    }

    @NonNull
    public String getToken() {
        return token;
    }
}
