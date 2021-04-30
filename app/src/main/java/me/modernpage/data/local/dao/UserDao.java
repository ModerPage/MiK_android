package me.modernpage.data.local.dao;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.modernpage.data.local.entity.AuthResult;
import me.modernpage.data.local.entity.FollowRequest;
import me.modernpage.data.local.entity.FollowerLoadResult;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.MemberLoadResult;
import me.modernpage.data.local.entity.Profile;

@Dao
public abstract class UserDao {
    private static final String TAG = "UserDao";

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(AuthResult authResult);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(MemberLoadResult result);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(LoadResult result);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(FollowerLoadResult result);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract long insert(Profile profile);

    @Query("select * from PROFILE where PID=:id")
    public abstract LiveData<Profile> getProfileLDById(long id);

    @Query("select * from PROFILE where PID=:id")
    public abstract Profile getProfileById(long id);

    @Query("select * from AUTH_RESULT where USERNAME=:username")
    public abstract LiveData<AuthResult> getAuthResultLD(String username);

    @Query("select * from AUTH_RESULT where USERNAME=:username")
    public abstract AuthResult getAuthResult(String username);

    @Query("delete from AUTH_RESULT where TOKEN=:token")
    public abstract void deleteAuthResult(String token);

    @Query("update AUTH_RESULT set TOKEN=:newToken where TOKEN=:currentToken")
    public abstract void updateToken(String currentToken, String newToken);

    @Query("delete from AUTH_RESULT")
    public abstract void deleteAllAuthResult();

    @Query("select * from MEMBER_LOAD_RESULT where URL =:url")
    public abstract LiveData<MemberLoadResult> getMemberLoadResultLD(String url);

    @Query("select * from MEMBER_LOAD_RESULT where URL =:url")
    public abstract MemberLoadResult getMemberLoadResult(String url);

    @Query("select * from LOAD_RESULT where URL =:url")
    public abstract LiveData<LoadResult> getUserLoadResultLD(String url);

    @Query("select * from LOAD_RESULT where URL =:url")
    public abstract LoadResult getUserLoadResult(String url);

    @Query("select * from FOLLOWER_LOAD_RESULT where URL =:url")
    public abstract LiveData<FollowerLoadResult> getFollowerLoadResultLD(String url);

    @Query("select * from FOLLOWER_LOAD_RESULT where URL =:url")
    public abstract FollowerLoadResult getFollowerLoadResult(String url);

    public LiveData<List<Profile>> loadOrderedUsers(List<Long> userIds) {
        Map<Long, Integer> order = new HashMap<>();
        int index = 0;
        for (Long userId : userIds) {
            order.put(userId, index++);
        }
        return Transformations.map(loadPostById(userIds), user -> {
            Collections.sort(user, (p1, p2) -> {
                int pos1 = order.get(p1.getId());
                int pos2 = order.get(p2.getId());
                return (pos1 - pos2);
            });
            return user;
        });
    }

    @Transaction
    @Query("select * from PROFILE where PID in (:userIds)")
    protected abstract LiveData<List<Profile>> loadPostById(List<Long> userIds);

    @Query("select * from FOLLOW_REQUEST where USER_ID=:uid and FOLLOWER_ID=:followerId")
    public abstract FollowRequest getFollowRequest(long uid, long followerId);


    @Delete
    public abstract void deleteLoadResult(MemberLoadResult membersLoadResult);
}
