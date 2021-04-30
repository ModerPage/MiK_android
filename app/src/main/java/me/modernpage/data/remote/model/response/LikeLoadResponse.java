package me.modernpage.data.remote.model.response;

import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.data.local.entity.Like;

public class LikeLoadResponse {

    @SerializedName("content")
    private List<Like> likes;
    private Integer nextPage;
    @SerializedName("totalElements")
    private Integer total;
    private Boolean liked;

    public List<Like> getLikes() {
        return likes;
    }

    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    public Integer getNextPage() {
        return nextPage;
    }

    public void setNextPage(Integer nextPage) {
        this.nextPage = nextPage;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(Boolean liked) {
        this.liked = liked;
    }

    @NonNull
    public List<Long> getLikeIds() {
        List<Long> likeIds = new ArrayList<>();
        for (Like like : likes) {
            likeIds.add(like.getId());
        }
        return likeIds;
    }

    @Override
    public String toString() {
        return "LikeLoadResponse{" +
                "likes=" + likes +
                ", nextPage=" + nextPage +
                ", totalCounts=" + total +
                ", liked=" + liked +
                '}';
    }
}
