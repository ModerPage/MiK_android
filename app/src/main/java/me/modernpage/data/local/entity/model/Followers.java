package me.modernpage.data.local.entity.model;

import androidx.annotation.Nullable;

import java.util.List;

import me.modernpage.data.local.entity.Profile;

public class Followers extends LoadModel<Profile> {
    @Nullable
    public Boolean followed;


    public Followers(List<Profile> contents, int total, @Nullable Boolean followed) {
        super(contents, total);
        this.followed = followed;
    }

    @Nullable
    public Boolean getFollowed() {
        return followed;
    }

    public void setFollowed(@Nullable Boolean followed) {
        this.followed = followed;
    }
}
