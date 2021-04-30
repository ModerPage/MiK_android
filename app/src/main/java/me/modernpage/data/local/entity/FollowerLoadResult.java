package me.modernpage.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.List;

@Entity(tableName = "FOLLOWER_LOAD_RESULT", inheritSuperIndices = true)
public class FollowerLoadResult extends LoadResult {

    @Nullable
    @ColumnInfo(name = "FOLLOWED")
    private Boolean followed;

//    @Nullable
//    @ColumnInfo(name = "REQUESTED")
//    private Boolean requested;

    public FollowerLoadResult() {
    }

    public FollowerLoadResult(@NonNull String url,
                              List<Long> userIds,
                              @Nullable Integer next,
                              int total, @Nullable Boolean followed) {
        super(url, userIds, next, total);
        this.followed = followed;
//        this.requested = requested;
    }

    @Nullable
    public Boolean getFollowed() {
        return followed;
    }

    public void setFollowed(@Nullable Boolean followed) {
        this.followed = followed;
    }

//    @Nullable
//    public Boolean getRequested() {
//        return requested;
//    }
//
//    public void setRequested(@Nullable Boolean requested) {
//        this.requested = requested;
//    }
}
