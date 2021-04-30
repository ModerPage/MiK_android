package me.modernpage.data.local.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import java.util.List;

@Entity(primaryKeys = {"URL"}, tableName = "LOAD_RESULT")
public class LoadResult {
    @NonNull
    @ColumnInfo(name = "URL")
    private String url;

    @ColumnInfo(name = "IDs")
    private List<Long> ids;

    @Nullable
    @ColumnInfo(name = "NEXT")
    private Integer next;

    @ColumnInfo(name = "TOTAL")
    private int total;

    @Ignore
    public LoadResult() {
    }

    public LoadResult(@NonNull String url, List<Long> ids, @Nullable Integer next, int total) {
        this.url = url;
        this.ids = ids;
        this.next = next;
        this.total = total;
    }

    @NonNull
    public String getUrl() {
        return url;
    }

    public List<Long> getIds() {
        return ids;
    }

    @Nullable
    public Integer getNext() {
        return next;
    }

    public int getTotal() {
        return total;
    }

    public void setUrl(@NonNull String url) {
        this.url = url;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }

    public void setNext(@Nullable Integer next) {
        this.next = next;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
