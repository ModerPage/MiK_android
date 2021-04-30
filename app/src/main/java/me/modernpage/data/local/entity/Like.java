package me.modernpage.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "PLIKE", indices = {@Index("LID"), @Index("OWNER_ID"), @Index("POST_ID")},
        foreignKeys = {
                @ForeignKey(
                        entity = Profile.class,
                        parentColumns = "PID",
                        childColumns = "OWNER_ID")})
//        @ForeignKey(
//                entity = Post.class,
//                parentColumns = "PID",
//                childColumns = "POST_ID")})
public class Like {

    @PrimaryKey
    @ColumnInfo(name = "LID")
    private long id;

    @ColumnInfo(name = "CREATED")
    private Date created;

    @Ignore
    @Embedded
    private Profile owner;

    @Ignore
    @Embedded
    private Post post;

    @ColumnInfo(name = "OWNER_ID")
    private long ownerId;

    @ColumnInfo(name = "POST_ID")
    private long postId;

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public Like() {
    }

    public Like(long id, Date created, Profile owner, Post post) {
        this.id = id;
        this.created = created;
        this.owner = owner;
        this.post = post;
    }

    public void process() {
        ownerId = owner.getId();
//        postId = post.getId();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Profile getOwner() {
        return owner;
    }

    public void setOwner(Profile owner) {
        this.owner = owner;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    @Override
    public String toString() {
        return "Like{" +
                "id=" + id +
                ", created=" + created +
                ", owner=" + owner +
                ", post=" + post +
                ", ownerId=" + ownerId +
                ", postId=" + postId +
                '}';
    }
}
