package me.modernpage.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.util.List;

@Entity(tableName = "MEMBER_LOAD_RESULT", inheritSuperIndices = true)
public class MemberLoadResult extends LoadResult {
    @Nullable
    @ColumnInfo(name = "JOINED")
    public Boolean joined;

    @Ignore
    public MemberLoadResult() {
    }

    public MemberLoadResult(@NonNull String url, List<Long> ids, @Nullable Integer next, int total, @Nullable Boolean joined) {
        super(url, ids, next, total);
        this.joined = joined;
    }

    @Nullable
    public Boolean getJoined() {
        return joined;
    }

    public void setJoined(@Nullable Boolean joined) {
        this.joined = joined;
    }
}
