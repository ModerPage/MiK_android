package me.modernpage.data.local.entity.model;

import java.util.List;

import me.modernpage.data.local.entity.relation.LikeRelation;

public class Likes extends LoadModel<LikeRelation> {
    private Boolean liked;

    public Likes(List<LikeRelation> likes, int total, Boolean liked) {
        super(likes, total);
        this.liked = liked;
    }


    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    @Override
    public String toString() {
        return "Likes{" +
                "likes=" + getContents() +
                ", total=" + getTotal() +
                ", liked=" + liked +
                '}';
    }
}
