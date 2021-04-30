package me.modernpage.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "FOLLOW_REQUEST", indices = {@Index(value = {"USER_ID"}), @Index(value = {"FOLLOWER_ID"})},
        foreignKeys = {
                @ForeignKey(
                        entity = Profile.class,
                        parentColumns = "PID",
                        childColumns = "USER_ID"),
                @ForeignKey(
                        entity = Profile.class,
                        parentColumns = "PID",
                        childColumns = "FOLLOWER_ID")
        }, primaryKeys = {"USER_ID", "FOLLOWER_ID"})
public class FollowRequest {
    @ColumnInfo(name = "USER_ID")
    private long userId;
    @ColumnInfo(name = "FOLLOWER_ID")
    private long followerId;

    public FollowRequest(long userId, long followerId) {
        this.userId = userId;
        this.followerId = followerId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getFollowerId() {
        return followerId;
    }

    public void setFollowerId(long followerId) {
        this.followerId = followerId;
    }
}
