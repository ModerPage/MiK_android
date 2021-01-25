package me.modernpage.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

public class Post {

    private long postId;
    private UserEntity postOwner;
    private Location postLocation;
    private Collection<Comment> postComments;
    private Group postGroup;
    private Collection<Like> postLikes;
    private Date postedDate;
    private String postText;
    private String fileURL;

    public Post() {
    }

    public Post(UserEntity postOwner, Location postLocation, Group postGroup, String postText, String fileURL) {
        this.postOwner = postOwner;
        this.postLocation = postLocation;
        this.postGroup = postGroup;
        this.postText = postText;
        this.fileURL = fileURL;
        postComments = new ArrayList<>();
        postLikes = new ArrayList<>();
    }

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public UserEntity getPostOwner() {
        return postOwner;
    }

    public void setPostOwner(UserEntity postOwner) {
        this.postOwner = postOwner;
    }

    public Location getPostLocation() {
        return postLocation;
    }

    public void setPostLocation(Location postLocation) {
        this.postLocation = postLocation;
    }

    public Collection<Comment> getPostComments() {
        return postComments;
    }

    public void setPostComments(Collection<Comment> postComments) {
        this.postComments = postComments;
    }

    public Group getPostGroup() {
        return postGroup;
    }

    public void setPostGroup(Group postGroup) {
        this.postGroup = postGroup;
    }

    public Collection<Like> getPostLikes() {
        return postLikes;
    }

    public void setPostLikes(Collection<Like> postLikes) {
        this.postLikes = postLikes;
    }

    public Date getPostedDate() {
        return postedDate;
    }

    public void setPostedDate(Date postedDate) {
        this.postedDate = postedDate;
    }

    public String getPostText() {
        return postText;
    }

    public void setPostText(String postText) {
        this.postText = postText;
    }

    public String getFileURL() {
        return fileURL;
    }

    public void setFileURL(String fileURL) {
        this.fileURL = fileURL;
    }

}

