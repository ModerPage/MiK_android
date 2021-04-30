package me.modernpage.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import me.modernpage.data.local.entity.Limiter;

@Dao
public abstract class RateLimiterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(Limiter limiter);

    @Query("select * from LIMITER where KEY_ID=:key")
    public abstract Limiter findRateLimiterByKey(String key);

    @Query("delete from LIMITER where KEY_ID=:key")
    public abstract void delete(String key);
}
