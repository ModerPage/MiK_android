package me.modernpage.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "JOIN_GROUP", indices = {@Index("USER_ID"), @Index("GROUP_ID")},
        foreignKeys = {
                @ForeignKey(
                        entity = PrivateGroup.class,
                        parentColumns = "GID",
                        childColumns = "GROUP_ID",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(
                        entity = Profile.class,
                        parentColumns = "PID",
                        childColumns = "USER_ID",
                        onDelete = ForeignKey.CASCADE)
        }, primaryKeys = {"GROUP_ID", "USER_ID"})
public class JoinGroupResult {
    @ColumnInfo(name = "GROUP_ID")
    private long groupId;
    @ColumnInfo(name = "USER_ID")
    private long userId;
    @ColumnInfo(name = "JOINED")
    private boolean joined;

    public JoinGroupResult(long groupId, long userId, boolean joined) {
        this.groupId = groupId;
        this.userId = userId;
        this.joined = joined;
    }

    public long getGroupId() {
        return groupId;
    }

    public void setGroupId(long groupId) {
        this.groupId = groupId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public boolean isJoined() {
        return joined;
    }

    public void setJoined(boolean joined) {
        this.joined = joined;
    }
}
