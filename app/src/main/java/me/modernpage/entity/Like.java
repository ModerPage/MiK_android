package me.modernpage.entity;

import java.util.Date;

public class Like {

    private long likeId;
    private UserEntity likeOwner;
    private Post likedPost;
    private Date likedDate;

    public long getLikeId() {
        return likeId;
    }

    public void setLikeId(long likeId) {
        this.likeId = likeId;
    }

    public UserEntity getLikeOwner() {
        return likeOwner;
    }

    public void setLikeOwner(UserEntity likeOwner) {
        this.likeOwner = likeOwner;
    }

    public Post getLikedPost() {
        return likedPost;
    }

    public void setLikedPost(Post likedPost) {
        this.likedPost = likedPost;
    }

    public Date getLikedDate() {
        return likedDate;
    }

    public void setLikedDate(Date likedDate) {
        this.likedDate = likedDate;
    }

}
