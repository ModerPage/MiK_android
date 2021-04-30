package me.modernpage.data.remote.model.response;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.data.local.entity.Profile;

public class UserLoadResponse extends LoadResponse<Profile> {
    @NotNull
    @Override
    public List<Long> getIds() {
        List<Long> userIds = new ArrayList<>();
        for (Profile user : getContents()) {
            userIds.add(user.getId());
        }
        return userIds;
    }

}
