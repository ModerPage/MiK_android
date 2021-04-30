package me.modernpage.data.local.entity.relation;

import androidx.room.Embedded;
import androidx.room.Relation;

import me.modernpage.data.local.entity.Like;
import me.modernpage.data.local.entity.Profile;

public class LikeRelation {
    @Embedded
    private Like like;

    @Relation(
            entity = Profile.class,
            parentColumn = "OWNER_ID",
            entityColumn = "PID"
    )
    private Profile owner;

//    @Relation(
//            entity = Post.class,
//            parentColumn = "POST_ID",
//            entityColumn = "PID"
//    )
//    private Post post;

    public Like getLike() {
        return like;
    }

    public void setLike(Like like) {
        this.like = like;
    }

    public Profile getOwner() {
        return owner;
    }

    public void setOwner(Profile owner) {
        this.owner = owner;
    }
//
//    public Post getPost() {
//        return post;
//    }
//
//    public void setPost(Post post) {
//        this.post = post;
//    }

    @Override
    public String toString() {
        return "LikeOwnerPost{" +
                "like=" + like +
                ", owner=" + owner +
                '}';
    }
}
