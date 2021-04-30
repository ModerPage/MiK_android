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

import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.Image;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.data.local.entity.PublicGroup;

@Dao
public abstract class GroupDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(Group group);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(PublicGroup group);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(PrivateGroup group);


    public void insertPublicGroup(PublicGroup group) {
        group.process();
        insert(group);
    }

    public void insertGroups(List<Group> groups) {
        for (Group group : groups) {
            insertGroup(group);
        }
    }

    @Transaction
    @Query("select * from PUBLIC_GROUP")
    public abstract LiveData<List<PublicGroup>> loadPublicGroups();

    public void insertGroup(Group group) {
        group.process();
        insert(group);
    }

    @Transaction
    @Query("select * from PGROUP where GID =:id")
    public abstract LiveData<Group> getPublicGroupById(long id);

    @Transaction
    @Query("select * from PRIVATE_GROUP")
    public abstract LiveData<List<PrivateGroup>> loadPrivateGroups();


    public void insertPrivateGroup(PrivateGroup group) {
        group.process();
        insert(group);
    }

    public void insertPrivateGroups(List<PrivateGroup> privateGroups) {
        for (PrivateGroup privateGroup : privateGroups) {
            privateGroup.process();
            insert(privateGroup);
        }
    }

    public LiveData<List<Group>> loadOrderedGroups(List<Long> postIds) {
        Map<Long, Integer> order = new HashMap<>();
        int index = 0;
        for (Long postId : postIds) {
            order.put(postId, index++);
        }
        return Transformations.map(loadGroupsById(postIds), groups -> {
            Collections.sort(groups, (p1, p2) -> {
                int pos1 = order.get(p1.getId());
                int pos2 = order.get(p2.getId());
                return (pos1 - pos2);
            });
            return groups;
        });
    }

    @Transaction
    @Query("select * from PGROUP where GID in (:postIds)")
    protected abstract LiveData<List<Group>> loadGroupsById(List<Long> postIds);

    public LiveData<List<PrivateGroup>> loadOrderedPrivateGroups(List<Long> postIds) {
        Map<Long, Integer> order = new HashMap<>();
        int index = 0;
        for (Long postId : postIds) {
            order.put(postId, index++);
        }
        return Transformations.map(loadPrivateGroupsById(postIds), groups -> {
            Collections.sort(groups, (p1, p2) -> {
                int pos1 = order.get(p1.getId());
                int pos2 = order.get(p2.getId());
                return (pos1 - pos2);
            });
            return groups;
        });
    }

    @Transaction
    @Query("select * from PRIVATE_GROUP where GID in (:groupIds)")
    protected abstract LiveData<List<PrivateGroup>> loadPrivateGroupsById(List<Long> groupIds);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertImage(Image image);

    @Query("select * from LOAD_RESULT where URL=:url")
    public abstract LiveData<LoadResult> getLoadResultLD(String url);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(LoadResult loadResult);

    @Query("select * from LOAD_RESULT where URL=:url")
    public abstract LoadResult getLoadResult(String url);

    @Delete
    public abstract void delete(PrivateGroup privateGroup);

    @Delete
    public abstract void delete(Group group);

    public void insertPublicGroups(List<PublicGroup> groups) {
        for (PublicGroup group : groups) {
            insertPublicGroup(group);
        }
    }
}
