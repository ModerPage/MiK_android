package me.modernpage.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import java.util.List;

@Entity(tableName = "LIKE_LOAD_RESULT", inheritSuperIndices = true)
public class LikeLoadResult extends LoadResult {

    @Nullable
    @ColumnInfo(name = "LIKED")
    private Boolean liked;

    public LikeLoadResult(@NonNull String url, List<Long> ids, @Nullable Integer next, int total, @Nullable Boolean liked) {
        super(url, ids, next, total);
        this.liked = liked;
    }

    @Nullable
    public Boolean getLiked() {
        return liked;
    }

    public void setLiked(@Nullable Boolean liked) {
        this.liked = liked;
    }
}
