package me.modernpage.data.remote.model.response;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.data.local.entity.PrivateGroup;

public class GroupLoadResponse extends LoadResponse<PrivateGroup> {

    @NotNull
    @Override
    public List<Long> getIds() {
        List<Long> groupIds = new ArrayList<>();
        for (PrivateGroup group : getContents()) {
            groupIds.add(group.getId());
        }
        return groupIds;
    }
}
