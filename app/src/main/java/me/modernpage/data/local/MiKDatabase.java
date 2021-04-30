package me.modernpage.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import me.modernpage.data.local.dao.GroupDao;
import me.modernpage.data.local.dao.PostDao;
import me.modernpage.data.local.dao.RateLimiterDao;
import me.modernpage.data.local.dao.UserDao;
import me.modernpage.data.local.entity.AuthResult;
import me.modernpage.data.local.entity.Comment;
import me.modernpage.data.local.entity.File;
import me.modernpage.data.local.entity.FollowRequest;
import me.modernpage.data.local.entity.FollowerLoadResult;
import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.Image;
import me.modernpage.data.local.entity.Like;
import me.modernpage.data.local.entity.LikeLoadResult;
import me.modernpage.data.local.entity.Limiter;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.Location;
import me.modernpage.data.local.entity.MemberLoadResult;
import me.modernpage.data.local.entity.Post;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.PublicGroup;

@Database(entities = {Profile.class, AuthResult.class, Group.class, File.class, Image.class,
        PrivateGroup.class, Post.class, Like.class, Comment.class, Location.class, LoadResult.class,
        LikeLoadResult.class, Limiter.class, FollowRequest.class, FollowerLoadResult.class,
        PublicGroup.class, MemberLoadResult.class},
        version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MiKDatabase extends RoomDatabase {
    public abstract UserDao getUserDao();

    public abstract GroupDao getGroupDao();

    public abstract PostDao getPostDao();

    public abstract RateLimiterDao getRateLimiterDao();
}
