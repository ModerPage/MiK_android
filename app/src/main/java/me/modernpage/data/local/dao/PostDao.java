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

import me.modernpage.data.local.entity.Comment;
import me.modernpage.data.local.entity.File;
import me.modernpage.data.local.entity.Like;
import me.modernpage.data.local.entity.LikeLoadResult;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.Location;
import me.modernpage.data.local.entity.Post;
import me.modernpage.data.local.entity.relation.CommentRelation;
import me.modernpage.data.local.entity.relation.LikeRelation;
import me.modernpage.data.local.entity.relation.PostRelation;

@Dao
public abstract class PostDao {
    private static final String TAG = "PostDao";

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract long insert(Post post);

    public long insertPost(Post post) {
        post.process();
        return insert(post);
    }

    @Delete
    protected abstract void delete(Post post);

    public void deletePost(Post post) {
        deleteLikes(post.getId());
        deleteComments(post.getId());
        delete(post);
    }

    @Query("delete from PLIKE where POST_ID =:pid")
    protected abstract void deleteLikes(long pid);

    @Query("delete from PCOMMENT where POST_ID =:pid")
    protected abstract void deleteComments(long pid);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertLocation(Location location);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract void insert(Like like);

    public void insertLike(Like like) {
        like.process();
        insert(like);
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract void insert(Comment comment);

    public void insertComment(Comment comment) {
        comment.process();
        insert(comment);
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertCommentLoadResult(LoadResult loadResult);

    @Query("select * from LOAD_RESULT where URL =:url")
    public abstract LiveData<LoadResult> getCommentLoadResultLD(String url);

    @Query("select * from LOAD_RESULT where URL =:url")
    public abstract LoadResult getCommentLoadResult(String url);

    @Transaction
    @Query("select * from PCOMMENT where CID in (:commentIds)")
    protected abstract LiveData<List<CommentRelation>> loadCommentsByIds(List<Long> commentIds);

    public LiveData<List<CommentRelation>> loadOrderedComments(List<Long> commentIds) {
        Map<Long, Integer> order = new HashMap<>();
        int index = 0;
        for (Long commentId : commentIds) {
            order.put(commentId, index++);
        }
        return Transformations.map(loadCommentsByIds(commentIds), comments -> {
            Collections.sort(comments, (c1, c2) -> {
                int com1 = order.get(c1.getComment().getId());
                int com2 = order.get(c2.getComment().getId());
                return (com1 - com2);
            });
            return comments;
        });
    }


    @Query("select * from POST where PID == :id")
    public abstract Post getPostById(long id);

    public LiveData<List<PostRelation>> loadOrderedPosts(List<Long> postIds) {
        Map<Long, Integer> order = new HashMap<>();
        int index = 0;
        for (Long postId : postIds) {
            order.put(postId, index++);
        }
        return Transformations.map(loadPostById(postIds), posts -> {
            Collections.sort(posts, (p1, p2) -> {
                int pos1 = order.get(p1.getPost().getId());
                int pos2 = order.get(p2.getPost().getId());
                return (pos1 - pos2);
            });
            return posts;
        });
    }

    @Transaction
    @Query("select * from POST where PID in (:postIds)")
    protected abstract LiveData<List<PostRelation>> loadPostById(List<Long> postIds);

    @Query("select * from LOAD_RESULT where URL =:url")
    public abstract LoadResult getPostLoadResult(String url);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertPostLoadResult(LoadResult result);

    @Query("select * from LOAD_RESULT where URL =:url")
    public abstract LiveData<LoadResult> getPostLoadResultLD(String url);

    @Query("delete from PLIKE where LID=:id")
    public abstract int deleteLikeById(long id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertLikeLoadResult(LikeLoadResult likeLoadResult);

    @Query("select * from LIKE_LOAD_RESULT where URL =:url")
    public abstract LiveData<LikeLoadResult> getLikeLoadResultLD(String url);

    @Query("select * from LIKE_LOAD_RESULT where URL =:url")
    public abstract LikeLoadResult getLikeLoadResult(String url);

    public LiveData<List<LikeRelation>> loadOrderedLikes(List<Long> likeIds) {
        Map<Long, Integer> order = new HashMap<>();
        int index = 0;
        for (Long likeId : likeIds) {
            order.put(likeId, index++);
        }

        return Transformations.map(loadLikesByIds(likeIds), likes -> {
            Collections.sort(likes, (p1, p2) -> {
                int pos1 = order.get(p1.getLike().getId());
                int pos2 = order.get(p2.getLike().getId());
                return (pos1 - pos2);
            });
            return likes;
        });
    }

    @Transaction
    @Query("select * from PLIKE where LID in (:likeIds)")
    protected abstract LiveData<List<LikeRelation>> loadLikesByIds(List<Long> likeIds);

    @Delete
    public abstract void deleteLikeLoadResult(LikeLoadResult likeLoadResult);

    @Delete
    public abstract void deletePostLoadResult(LoadResult loadResult);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertFile(File file);

    @Delete
    public abstract void deleteComment(Comment comment);

    @Delete
    public abstract void deleteCommentLoadResult(LoadResult loadResult);

    @Transaction
    @Query("delete from POST where PID in (:ids)")
    public abstract void deletePosts(List<Long> ids);

    @Query("SELECT EXISTS(SELECT * FROM LOAD_RESULT WHERE URL = :url)")
    public abstract boolean existsLoadResult(String url);
}
