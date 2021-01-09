package me.modernpage.entity;

import java.util.Date;

public class Comment {

    private long commentId;
    private Post commentedPost;
    private UserEntity commentOwner;
    private String commentText;
    private Date commentDate;

    public long getCommentId() {
        return commentId;
    }

    public void setCommentId(long commentId) {
        this.commentId = commentId;
    }

    public Post getCommentedPost() {
        return commentedPost;
    }

    public void setCommentedPost(Post commentedPost) {
        this.commentedPost = commentedPost;
    }


    public UserEntity getCommentOwner() {
        return commentOwner;
    }

    public void setCommentOwner(UserEntity commentOwner) {
        this.commentOwner = commentOwner;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public Date getCommentDate() {
        return commentDate;
    }

    public void setCommentDate(Date commentDate) {
        this.commentDate = commentDate;
    }

}
