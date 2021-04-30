package me.modernpage.data.remote.model.response;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.data.local.entity.Comment;

public class CommentLoadResponse extends LoadResponse<Comment> {

    @NotNull
    @Override
    public List<Long> getIds() {
        List<Long> commentIds = new ArrayList<>();
        for (Comment comment : getContents()) {
            commentIds.add(comment.getId());
        }
        return commentIds;
    }
}
