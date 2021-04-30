package me.modernpage.data.local.entity.model;

import androidx.annotation.Nullable;

import java.util.List;

import me.modernpage.data.local.entity.Profile;

public class Members extends LoadModel<Profile> {
    @Nullable
    private Boolean joined;

    public Members(List<Profile> contents, int total, @Nullable Boolean joined) {
        super(contents, total);
        this.joined = joined;
    }

    @Nullable
    public Boolean getJoined() {
        return joined;
    }

    public void setJoined(@Nullable Boolean joined) {
        this.joined = joined;
    }
}
