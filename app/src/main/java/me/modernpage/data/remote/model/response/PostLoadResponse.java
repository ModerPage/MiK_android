package me.modernpage.data.remote.model.response;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.data.local.entity.Post;

public class PostLoadResponse extends LoadResponse<Post> {

    @NotNull
    @Override
    public List<Long> getIds() {
        List<Long> postIds = new ArrayList<>();
        for (Post post : getContents()) {
            postIds.add(post.getId());
        }
        return postIds;
    }
}
