package me.modernpage.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "LIMITER", indices = {@Index("KEY_ID")})
public class Limiter {
    @PrimaryKey
    @ColumnInfo(name = "KEY_ID")
    @NonNull
    private final String key;
    @ColumnInfo(name = "FETCH_TIME")
    private final long fetchTime;


    public Limiter(@NotNull String key, long fetchTime) {
        this.key = key;
        this.fetchTime = fetchTime;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    public long getFetchTime() {
        return fetchTime;
    }

}
