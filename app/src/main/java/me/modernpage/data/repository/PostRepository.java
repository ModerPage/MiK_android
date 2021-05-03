package me.modernpage.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import me.modernpage.data.local.MiKDatabase;
import me.modernpage.data.local.dao.PostDao;
import me.modernpage.data.local.entity.Comment;
import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.Like;
import me.modernpage.data.local.entity.LikeLoadResult;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.Location;
import me.modernpage.data.local.entity.Post;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.model.Likes;
import me.modernpage.data.local.entity.model.LoadModel;
import me.modernpage.data.local.entity.relation.CommentRelation;
import me.modernpage.data.local.entity.relation.PostRelation;
import me.modernpage.data.remote.ApiResponse;
import me.modernpage.data.remote.model.response.CommentLoadResponse;
import me.modernpage.data.remote.model.response.LikeLoadResponse;
import me.modernpage.data.remote.model.response.PostLoadResponse;
import me.modernpage.data.remote.resource.PostResource;
import me.modernpage.util.AbsentLiveData;
import me.modernpage.util.AppExecutors;
import me.modernpage.util.NetworkBoundResource;
import me.modernpage.util.Resource;
import me.modernpage.util.ResponseUtil;

public class PostRepository {
    private static final String TAG = "PostRepository";
    private final PostResource postResource;
    private final PostDao postDao;
    private final AppExecutors mAppExecutors;
    private final MiKDatabase mDatabase;

    @Inject
    public PostRepository(PostResource postResource, PostDao postDao, AppExecutors appExecutors, MiKDatabase database) {
        this.postResource = postResource;
        this.postDao = postDao;
        mAppExecutors = appExecutors;
        mDatabase = database;
    }

    public LiveData<Resource<Boolean>> likeControl(String url, long postId, long ownerId) {
        PostTask.LikeControl likeControl = PostTask.likeControl(url, postId, ownerId, mDatabase, postResource);
        mAppExecutors.networkIO().execute(likeControl);
        return likeControl.getLiveData();
    }

    public LiveData<Resource<Likes>> getPostLikes(String url, long ownerId) {
        return new NetworkBoundResource<Likes, ResponseUtil<LikeLoadResponse>>(mAppExecutors) {

            @Override
            protected void saveCallResult(@NonNull ResponseUtil<LikeLoadResponse> item) {
                LikeLoadResponse data = item.getData();
                if (data.getTotal() == 0)
                    return;
                List<Long> likeIds = data.getLikeIds();
                LikeLoadResult likeLoadResult = new LikeLoadResult(url, likeIds,
                        data.getNextPage(), data.getTotal(), data.getLiked());
                mDatabase.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        for (Like like : data.getLikes()) {
                            Profile owner = like.getOwner();
                            mDatabase.getUserDao().insert(owner);
                            postDao.insertLike(like);
                        }
                        postDao.insertLikeLoadResult(likeLoadResult);
                    }
                });
            }

            @Override
            protected boolean shouldFetch(@Nullable Likes data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<Likes> loadFromDb() {
                return Transformations.switchMap(postDao.getLikeLoadResultLD(url), input -> {
                    if (input == null)
                        return AbsentLiveData.create();

                    return Transformations.switchMap(postDao.loadOrderedLikes(input.getIds()), result -> {
                        if (result == null)
                            return AbsentLiveData.create();
                        return new MutableLiveData<>(new Likes(result, input.getTotal(), input.getLiked()));
                    });
                });
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResponseUtil<LikeLoadResponse>>> createCall() {
                return postResource.getLikes(url, ownerId);
            }

            @Override
            protected ResponseUtil<LikeLoadResponse> processResponse(ApiResponse<ResponseUtil<LikeLoadResponse>> response) {
                ResponseUtil<LikeLoadResponse> body = response.body;
                if (body != null)
                    body.getData().setNextPage(response.getNextPage());
                return body;
            }
        }.asLiveData();
    }

    public LiveData<Resource<LoadModel<CommentRelation>>> getPostComments(String url) {
        return new NetworkBoundResource<LoadModel<CommentRelation>, ResponseUtil<CommentLoadResponse>>(mAppExecutors) {

            @Override
            protected void saveCallResult(@NonNull ResponseUtil<CommentLoadResponse> item) {
                Log.d(TAG, "saveCallResult: comment load response: " + item.getData());
                CommentLoadResponse data = item.getData();
                if (data.getTotal() == 0)
                    return;
                List<Long> commentIds = data.getIds();
                LoadResult result =
                        new LoadResult(url, commentIds, data.getNextPage(), data.getTotal());
                mDatabase.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        for (Comment comment : data.getContents()) {

                            Profile owner = comment.getOwner();
                            mDatabase.getUserDao().insert(owner);
//                            postDao.insertPost(comment.getPost());
                            postDao.insertComment(comment);
                        }
                        postDao.insertCommentLoadResult(result);
                    }
                });
            }

            @Override
            protected boolean shouldFetch(@Nullable LoadModel<CommentRelation> data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<LoadModel<CommentRelation>> loadFromDb() {
                return Transformations.switchMap(postDao.getCommentLoadResultLD(url), input -> {
                    if (input == null)
                        return AbsentLiveData.create();
                    return Transformations.switchMap(postDao.loadOrderedComments(input.getIds()), result -> {
                        if (result == null)
                            return AbsentLiveData.create();
                        return new MutableLiveData<>(new LoadModel<>(result, input.getTotal()));
                    });
                });
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResponseUtil<CommentLoadResponse>>> createCall() {
                return postResource.getComments(url);
            }

            @Override
            protected ResponseUtil<CommentLoadResponse> processResponse(ApiResponse<ResponseUtil<CommentLoadResponse>> response) {
                ResponseUtil<CommentLoadResponse> body = response.body;
                if (body != null)
                    body.getData().setNextPage(response.getNextPage());
                return body;
            }
        }.asLiveData();
    }

    /**
     * @param url  post load result primary key
     * @param post deleting post
     * @return
     */
    public LiveData<Resource<Boolean>> deletePost(String url, Post post) {
        Log.d(TAG, "deletePost: url: " + url + ", post: " + post);
        PostTask.DeletePost deletePost = PostTask.deletePost(url, post, mDatabase, postResource);
        mAppExecutors.networkIO().execute(deletePost);
        return deletePost.getLiveData();
    }

    public LiveData<Resource<LoadModel<PostRelation>>> getPosts(String url) {
        return new NetworkBoundResource<LoadModel<PostRelation>, ResponseUtil<PostLoadResponse>>(mAppExecutors) {

            @Override
            protected void saveCallResult(@NonNull ResponseUtil<PostLoadResponse> item) {
                PostLoadResponse data = item.getData();
                if (data.getTotal() == 0)
                    return;
                List<Long> postIds = data.getIds();
                LoadResult postLoadResult = new LoadResult(url, postIds, data.getNextPage(), data.getTotal());
                mDatabase.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        for (Post post : data.getContents()) {

                            Location location = post.getLocation();
                            if (location != null)
                                postDao.insertLocation(location);

                            Group group = post.getGroup();
                            mDatabase.getGroupDao().insertGroup(group);

                            Profile owner = post.getOwner();
                            mDatabase.getUserDao().insert(owner);

                            postDao.insertFile(post.getFile());

                            postDao.insertPost(post);
                        }
                        postDao.insertPostLoadResult(postLoadResult);
                    }
                });
            }

            @Override
            protected boolean shouldFetch(@Nullable LoadModel<PostRelation> data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<LoadModel<PostRelation>> loadFromDb() {
                return Transformations.switchMap(postDao.getPostLoadResultLD(url), input -> {
                    if (input == null)
                        return AbsentLiveData.create();
                    return Transformations.switchMap(postDao.loadOrderedPosts(input.getIds()), result -> {
                        if (result == null)
                            return AbsentLiveData.create();
                        return new MutableLiveData<>(new LoadModel<>(result, input.getTotal()));
                    });
                });

            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResponseUtil<PostLoadResponse>>> createCall() {
                return postResource.getPosts(url);
            }

            @Override
            protected ResponseUtil<PostLoadResponse> processResponse(ApiResponse<ResponseUtil<PostLoadResponse>> response) {
                ResponseUtil<PostLoadResponse> body = response.body;
                if (body != null)
                    body.getData().setNextPage(response.getNextPage());
                return body;
            }
        }.asLiveData();
    }

    public LiveData<Resource<LoadModel<PostRelation>>> getPostLoads(String url) {
        return null;
    }

    public LiveData<Resource<Boolean>> loadPostsNextPage(String url) {
        PostTask.FetchNextPage fetchNextPageTask = PostTask.fetchNextPage(
                url, postResource, mDatabase);
        mAppExecutors.networkIO().execute(fetchNextPageTask);
        return fetchNextPageTask.getLiveData();
    }

    public LiveData<Resource<Boolean>> createPost(String postLoadResultUrl, String postsUrl, Post post, File file) {
        PostTask.CreatePost createPost = PostTask.createPost(postLoadResultUrl, postsUrl, post, file, mDatabase, postResource);
        mAppExecutors.networkIO().execute(createPost);
        return createPost.getLiveData();
    }

    public LiveData<Resource<Boolean>> updatePost(String postUrl, Post post, File file) {
        PostTask.UpdatePost updatePost = PostTask.updatePost(postUrl, post, file, mDatabase, postResource);
        mAppExecutors.networkIO().execute(updatePost);
        return updatePost.getLiveData();
    }

    public LiveData<Resource<Boolean>> hidePost(String url, long uid, Post post) {
        PostTask.HidePost hidePost = PostTask.hidePost(url, uid, post, mDatabase, postResource);
        mAppExecutors.networkIO().execute(hidePost);
        return hidePost.getLiveData();
    }

    public LiveData<Resource<Boolean>> refreshPosts(String url) {
        PostTask.Refresh refresh = PostTask.refreshPosts(url, postResource, mDatabase);
        mAppExecutors.networkIO().execute(refresh);
        return refresh.getLiveData();
    }

    public LiveData<Resource<Boolean>> loadCommentsNextPage(String url) {
        PostTask.FetchCommentNextPage fetchCommentNextPage = PostTask.fetchCommentNextPage(
                url, postResource, mDatabase);
        mAppExecutors.networkIO().execute(fetchCommentNextPage);
        return fetchCommentNextPage.getLiveData();
    }

    public LiveData<Resource<Boolean>> refreshComments(String url) {
        PostTask.RefreshComments refreshComments = PostTask.refreshComments(url, postResource, mDatabase);
        mAppExecutors.networkIO().execute(refreshComments);
        return refreshComments.getLiveData();
    }

    public LiveData<Resource<Boolean>> saveComment(String url, Comment comment) {
        PostTask.AddComment addComment = PostTask.addComment(url, comment, mDatabase, postResource);
        mAppExecutors.networkIO().execute(addComment);
        return addComment.getLiveData();
    }

    public LiveData<Resource<Boolean>> deleteComment(String url, Comment comment) {
        PostTask.DeleteComment deleteComment = PostTask.deleteComment(url, comment, mDatabase, postResource);
        mAppExecutors.networkIO().execute(deleteComment);
        return deleteComment.getLiveData();
    }
}
