package me.modernpage.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;


@Entity(tableName = "PCOMMENT", indices = {@Index("CID"), @Index("OWNER_ID"), @Index("POST_ID")},
        foreignKeys = {
                @ForeignKey(
                        entity = Profile.class,
                        parentColumns = "PID",
                        childColumns = "OWNER_ID"),
//            @ForeignKey(
//                    entity = Post.class,
//                    parentColumns = "PID",
//                    childColumns = "POST_ID")
        })
public class Comment implements Serializable {
    @PrimaryKey
    @ColumnInfo(name = "CID")
    private long id;
    @ColumnInfo(name = "TEXT")
    private String text;
    @ColumnInfo(name = "CREATED")
    private Date created;

    @Ignore
    @Embedded
    private Post post;

    @Ignore
    @Embedded
    private Profile owner;

    @ColumnInfo(name = "OWNER_ID")
    private long ownerId;

    @ColumnInfo(name = "POST_ID")
    private long postId;

    public Comment() {
    }

    public Comment(long id, String text, Date created, Post post, Profile owner) {
        this.id = id;
        this.text = text;
        this.created = created;
        this.post = post;
        this.owner = owner;
    }

    public void process() {
        ownerId = owner.getId();
//        postId = post.getId();
    }

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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Profile getOwner() {
        return owner;
    }

    public void setOwner(Profile owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", created=" + created +
                ", post=" + post +
                ", owner=" + owner +
                ", ownerId=" + ownerId +
                '}';
    }
}
