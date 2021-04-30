package me.modernpage.data.repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.modernpage.data.local.MiKDatabase;
import me.modernpage.data.local.entity.Comment;
import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.Like;
import me.modernpage.data.local.entity.LikeLoadResult;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.Location;
import me.modernpage.data.local.entity.Post;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.remote.ApiResponse;
import me.modernpage.data.remote.model.response.CommentLoadResponse;
import me.modernpage.data.remote.model.response.PostLoadResponse;
import me.modernpage.data.remote.resource.PostResource;
import me.modernpage.util.Constants;
import me.modernpage.util.Resource;
import me.modernpage.util.ResponseUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class PostTask {
    /**
     * @param url          primary key for PostLoadResult
     * @param post         entity data for deletion
     * @param database     app database
     * @param postResource api network controller
     * @return
     */
    public static DeletePost deletePost(String url, Post post,
                                        MiKDatabase database, PostResource postResource) {
        return new DeletePost(url, post, database, postResource);
    }

    public static LikeControl likeControl(String url, long postId, long ownerId,
                                          MiKDatabase database, PostResource postResource) {
        return new PostTask.LikeControl(url, postId, ownerId, database, postResource);
    }

    public static UpdateTask updatePost(String postUrl, Post post, File file, MiKDatabase database, PostResource postResource) {
        return new UpdateTask(postUrl, post, file, database, postResource);
    }

    public static FetchNextPage fetchNextPage(String url, PostResource postResource, MiKDatabase database) {
        return new FetchNextPage(url, postResource, database);
    }

    public static HidePost hidePost(String url, long uid, Post post, MiKDatabase database, PostResource postResource) {
        return new HidePost(database, postResource, url, post, uid);
    }

    public static Refresh refreshPosts(String url, PostResource postResource, MiKDatabase database) {
        return new Refresh(database, postResource, url);
    }

    public static FetchCommentNextPage fetchCommentNextPage(String url, PostResource postResource, MiKDatabase database) {
        return new FetchCommentNextPage(url, postResource, database);
    }

    public static RefreshComments refreshComments(String url, PostResource postResource, MiKDatabase database) {
        return new RefreshComments(database, postResource, url);
    }

    public static AddComment addComment(String url, Comment comment, MiKDatabase database, PostResource postResource) {
        return new AddComment(database, postResource, url, comment);
    }

    public static DeleteComment deleteComment(String url, Comment comment, MiKDatabase database, PostResource postResource) {
        return new DeleteComment(url, comment, database, postResource);
    }

    static class DeletePost implements Runnable {
        private static final String TAG = "DeletePost";
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final MiKDatabase mDatabase;
        private final PostResource mPostResource;
        private final String mUrl;
        private final Post mPost;

        DeletePost(String url, Post post, MiKDatabase database, PostResource postResource) {
            mUrl = url;
            mPost = post;
            mDatabase = database;
            mPostResource = postResource;
        }

        @Override
        public void run() {
            boolean existed = mDatabase.getPostDao().existsLoadResult(mUrl);
            if (!existed) {
                mLiveData.postValue(null);
                return;
            }
            try {
                Response<ResponseUtil<Void>> deleteResponse = mPostResource.deletePost(mPost._self()).execute();
                ApiResponse<ResponseUtil<Void>> apiResponse = new ApiResponse<ResponseUtil<Void>>(deleteResponse);
                if (apiResponse.isSuccessful()) {

                    LoadResult postLoadResult = mDatabase.getPostDao().getPostLoadResult(Constants.Network.ENDPOINT_POSTS);
                    if (postLoadResult != null) {
                        if (postLoadResult.getIds().remove(mPost.getId())) {
                            postLoadResult.setTotal(postLoadResult.getTotal() - 1);
                        }
                    }

                    LoadResult myPostLoadResult = mDatabase.getPostDao().getPostLoadResult(mPost.getOwner()._posts());
                    if (myPostLoadResult != null) {
                        if (myPostLoadResult.getIds().remove(mPost.getId())) {
                            myPostLoadResult.setTotal(myPostLoadResult.getTotal() - 1);
                        }
                    }

                    LoadResult groupPostLoadResult = mDatabase.getGroupDao().getLoadResult(mPost.getGroup()._posts());
                    if (groupPostLoadResult != null) {
                        if (groupPostLoadResult.getIds().remove(mPost.getId())) {
                            groupPostLoadResult.setTotal(groupPostLoadResult.getTotal() - 1);
                        }
                    }

                    mDatabase.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            mDatabase.getPostDao().deletePost(mPost);
                            if (postLoadResult != null) {
                                if (postLoadResult.getIds().isEmpty())
                                    mDatabase.getPostDao().deletePostLoadResult(postLoadResult);
                                else
                                    mDatabase.getPostDao().insertPostLoadResult(postLoadResult);
                            }

                            if (groupPostLoadResult != null) {
                                if (groupPostLoadResult.getIds().isEmpty())
                                    mDatabase.getPostDao().deletePostLoadResult(groupPostLoadResult);
                                else
                                    mDatabase.getPostDao().insertPostLoadResult(groupPostLoadResult);
                            }

                            if (myPostLoadResult != null) {
                                if (myPostLoadResult.getIds().isEmpty())
                                    mDatabase.getPostDao().deletePostLoadResult(myPostLoadResult);
                                else
                                    mDatabase.getPostDao().insertPostLoadResult(myPostLoadResult);
                            }
                        }
                    });
                    mLiveData.postValue(Resource.success(true));
                } else {
                    if (apiResponse.isNotVerified())
                        mLiveData.postValue(Resource.logout(apiResponse.errorMessage, false));
                    else
                        mLiveData.postValue(Resource.error(apiResponse.errorMessage, false));
                }
            } catch (IOException e) {
                mLiveData.postValue(Resource.error(e.getMessage(), false));
            }
        }

        LiveData<Resource<Boolean>> getLiveData() {
            return mLiveData;
        }
    }

    static class FetchNextPage implements Runnable {
        private static final String TAG = "FetchNextPageTask";
        private final MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();
        private final String url;
        private final PostResource postResource;
        private final MiKDatabase database;

        FetchNextPage(String url, PostResource postResource, MiKDatabase database) {
            this.url = url;
            this.postResource = postResource;
            this.database = database;
        }

        @Override
        public void run() {
            LoadResult current = database.getPostDao().getPostLoadResult(url);
            if (current == null) {
                liveData.postValue(null);
                return;
            }
            final Integer nextPage = current.getNext();
            if (nextPage == null) {
                liveData.postValue(Resource.success(false));
                return;
            }
            try {
                Response<ResponseUtil<PostLoadResponse>> response = postResource.getPosts(url, nextPage).execute();
                ApiResponse<ResponseUtil<PostLoadResponse>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    // we merge all repo ids into 1 list so that it is easier to fetch the result list.
                    List<Long> ids = new ArrayList<>();
                    ids.addAll(current.getIds());
                    ids.addAll(apiResponse.body.getData().getIds());
                    LoadResult merged = new LoadResult(url, ids, apiResponse.getNextPage(), apiResponse.body.getData().getTotal());
                    database.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            for (Post post : apiResponse.body.getData().getContents()) {
                                Location location = post.getLocation();
                                if (location != null)
                                    database.getPostDao().insertLocation(location);

                                Profile owner = post.getOwner();
                                database.getUserDao().insert(owner);

                                Group group = post.getGroup();
                                database.getGroupDao().insertGroup(group);

                                database.getPostDao().insertFile(post.getFile());

                                database.getPostDao().insertPost(post);
                            }
                            database.getPostDao().insertPostLoadResult(merged);
                        }
                    });
                    liveData.postValue(Resource.success(apiResponse.getNextPage() != null));
                } else {
                    if (apiResponse.isNotVerified())
                        liveData.postValue(Resource.logout(apiResponse.errorMessage, true));
                    else
                        liveData.postValue(Resource.error(apiResponse.errorMessage, true));
                }
            } catch (IOException e) {
                liveData.postValue(Resource.error(e.getMessage(), true));
            }
        }

        LiveData<Resource<Boolean>> getLiveData() {
            return liveData;
        }
    }

    static class LikeControl implements Runnable {
        private static final String TAG = "LikeControl";
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final MiKDatabase mDatabase;
        private final PostResource mPostResource;
        private final String mUrl;
        private final long mOwnerId;
        private final long mPostId;

        LikeControl(String url, long postId, long ownerId, MiKDatabase database, PostResource postResource) {
            mUrl = url;
            mOwnerId = ownerId;
            mPostId = postId;
            mDatabase = database;
            mPostResource = postResource;
        }

        @Override
        public void run() {
            LikeLoadResult result = mDatabase.getPostDao().getLikeLoadResult(mUrl);
            if (result == null || (result.getLiked() != null && !result.getLiked())) {
                // process of liking
                Profile owner = mDatabase.getUserDao().getProfileById(mOwnerId);
                Post post = mDatabase.getPostDao().getPostById(mPostId);
                Like unsaved_like = new Like();
                unsaved_like.setOwner(owner);
                unsaved_like.setPost(post);
                try {
                    Response<ResponseUtil<Like>> likeResponse = mPostResource.saveLike(mUrl, unsaved_like).execute();
                    ApiResponse<ResponseUtil<Like>> apiResponse = new ApiResponse<>(likeResponse);
                    if (apiResponse.isSuccessful()) {
                        if (result == null) {
                            result = new LikeLoadResult(mUrl, Collections.singletonList(
                                    apiResponse.body.getData().getId()), null, 1, true);
                        } else {
                            result.getIds().add(0, apiResponse.body.getData().getId());
                            result.setTotal(result.getTotal() + 1);
                            result.setLiked(true);
                        }
                        LikeLoadResult finalResult = result;
                        mDatabase.runInTransaction(new Runnable() {
                            @Override
                            public void run() {
                                mDatabase.getPostDao().insertLike(apiResponse.body.getData());
                                mDatabase.getPostDao().insertLikeLoadResult(finalResult);
                            }
                        });
                        mLiveData.postValue(Resource.success(true));
                    } else {
                        if (apiResponse.isNotVerified())
                            mLiveData.postValue(Resource.logout(apiResponse.errorMessage, false));
                        else
                            mLiveData.postValue(Resource.error(apiResponse.errorMessage, false));
                    }
                } catch (IOException e) {
                    mLiveData.postValue(Resource.error(e.getMessage(), false));
                }
            } else {
                // process of un liking
                try {
                    Response<ResponseUtil<Like>> likeResponse = mPostResource.deleteLike(mUrl, mOwnerId).execute();
                    ApiResponse<ResponseUtil<Like>> apiResponse = new ApiResponse<>(likeResponse);
                    if (apiResponse.isSuccessful()) {
                        result.getIds().remove(apiResponse.body.getData().getId());
                        result.setLiked(false);
                        result.setTotal(result.getTotal() - 1);
                        LikeLoadResult finalResult1 = result;
                        mDatabase.runInTransaction(new Runnable() {
                            @Override
                            public void run() {
                                mDatabase.getPostDao().deleteLikeById(apiResponse.body.getData().getId());
                                if (finalResult1.getIds().isEmpty()) {
                                    mDatabase.getPostDao().deleteLikeLoadResult(finalResult1);
                                } else {
                                    mDatabase.getPostDao().insertLikeLoadResult(finalResult1);
                                }
                            }
                        });
                        mLiveData.postValue(Resource.success(false));
                    } else {
                        if (apiResponse.isNotVerified())
                            mLiveData.postValue(Resource.logout(apiResponse.errorMessage, false));
                        else
                            mLiveData.postValue(Resource.error(apiResponse.errorMessage, false));
                    }
                } catch (IOException e) {
                    mLiveData.postValue(Resource.error(e.getMessage(), true));
                }
            }

        }

        public MutableLiveData<Resource<Boolean>> getLiveData() {
            return mLiveData;
        }
    }

    public static CreatePost createPost(String postLoadResultUrl, String postsUrl, Post post, File file, MiKDatabase miKDatabase, PostResource postResource) {
        return new CreatePost(postLoadResultUrl, postsUrl, post, file, miKDatabase, postResource);
    }

    static class CreatePost implements Runnable {
        private static final String TAG = "CreatePost";
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final MiKDatabase mDatabase;
        private final PostResource mPostResource;
        private final String mPostsUrl;
        private final String mPostLoadResultUrl;
        private final Post mPost;
        private final File mFile;

        public CreatePost(String postLoadResultUrl, String postsUrl, Post post, File file, MiKDatabase miKDatabase, PostResource postResource) {
            mPostLoadResultUrl = postLoadResultUrl;
            mPostsUrl = postsUrl;
            mDatabase = miKDatabase;
            mPostResource = postResource;
            mPost = post;
            mFile = file;
        }

        @Override
        public void run() {
            if (mFile == null) {
                mLiveData.postValue(null);
                return;
            }
            RequestBody postFile = RequestBody.create(mFile, MultipartBody.FORM);
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.addFormDataPart("file", "file", postFile);
            try {
                Response<ResponseUtil<Post>> response = mPostResource.savePost(mPostsUrl, mPost, builder.build()).execute();
                ApiResponse<ResponseUtil<Post>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    Post post = apiResponse.body.getData();

                    LoadResult postLoadResult = mDatabase.getPostDao().getPostLoadResult(Constants.Network.ENDPOINT_POSTS);
                    if (postLoadResult != null) {
                        postLoadResult.getIds().add(0, post.getId());
                        postLoadResult.setTotal(postLoadResult.getTotal() + 1);
                    } else {
                        postLoadResult = new LoadResult(mPostLoadResultUrl, Collections.singletonList(post.getId()), null, 1);
                    }

                    LoadResult myPostLoadResult = mDatabase.getPostDao().getPostLoadResult(mPost.getOwner()._posts());
                    if (myPostLoadResult != null) {
                        myPostLoadResult.getIds().add(0, post.getId());
                        myPostLoadResult.setTotal(myPostLoadResult.getTotal() + 1);
                    } else {
                        myPostLoadResult = new LoadResult(mPost.getOwner()._posts(), Collections.singletonList(post.getId()), null, 1);
                    }

                    LoadResult groupPostLoadResult = mDatabase.getGroupDao().getLoadResult(mPost.getGroup()._posts());
                    if (groupPostLoadResult != null) {
                        groupPostLoadResult.getIds().add(0, post.getId());
                        groupPostLoadResult.setTotal(groupPostLoadResult.getTotal() + 1);
                    } else {
                        groupPostLoadResult = new LoadResult(mPost.getGroup()._posts(), Collections.singletonList(post.getId()), null, 1);
                    }


                    LoadResult finalResult = postLoadResult;
                    LoadResult finalGroupPostLoadResult = groupPostLoadResult;
                    LoadResult finalMyPostLoadResult = myPostLoadResult;
                    mDatabase.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            if (post.getLocation() != null)
                                mDatabase.getPostDao().insertLocation(post.getLocation());
                            mDatabase.getPostDao().insertFile(post.getFile());
                            mDatabase.getPostDao().insertPost(post);
                            mDatabase.getPostDao().insertPostLoadResult(finalResult);
                            mDatabase.getGroupDao().insert(finalGroupPostLoadResult);
                            mDatabase.getPostDao().insertPostLoadResult(finalMyPostLoadResult);
                        }
                    });

                    if (!mPostLoadResultUrl.equals(Constants.Network.ENDPOINT_POSTS)) {
                        LoadResult homeLoad = mDatabase.getPostDao().getPostLoadResult(Constants.Network.ENDPOINT_POSTS);
                        if (homeLoad != null) {
                            homeLoad.getIds().add(0, post.getId());
                            homeLoad.setTotal(homeLoad.getTotal() + 1);
                        } else {
                            homeLoad = new LoadResult(Constants.Network.ENDPOINT_POSTS, Collections.singletonList(post.getId()), null, 1);
                        }
                        mDatabase.getPostDao().insertPostLoadResult(homeLoad);
                    }
                    mLiveData.postValue(Resource.success(true));
                } else {
                    if (apiResponse.isNotVerified()) {
                        mLiveData.postValue(Resource.logout(apiResponse.errorMessage, false));
                    } else {
                        mLiveData.postValue(Resource.error(apiResponse.errorMessage, false));
                    }
                }
            } catch (IOException e) {
                mLiveData.postValue(Resource.error(e.getMessage(), false));
            }
        }

        public MutableLiveData<Resource<Boolean>> getLiveData() {
            return mLiveData;
        }
    }

    static class UpdateTask implements Runnable {
        private static final String TAG = "UpdateTask";
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final MiKDatabase mDatabase;
        private final PostResource mPostResource;
        private final String mPostsUrl;
        private final Post mPost;
        private final File mFile;

        UpdateTask(String postUrl, Post post, File file, MiKDatabase database, PostResource postResource) {
            mPostsUrl = postUrl;
            mDatabase = database;
            mPostResource = postResource;
            mPost = post;
            mFile = file;
        }

        @Override
        public void run() {
            Call<ResponseUtil<Post>> call;
            if (mFile != null) {
                RequestBody postFile = RequestBody.create(mFile, MultipartBody.FORM);
                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.addFormDataPart("file", "file", postFile);
                call = mPostResource.savePost(mPostsUrl, mPost, builder.build());
            } else {
                call = mPostResource.updatePost(mPostsUrl, mPost);
            }
            try {
                Response<ResponseUtil<Post>> response = call.execute();
                ApiResponse<ResponseUtil<Post>> apiResponse = new ApiResponse<ResponseUtil<Post>>(response);
                if (apiResponse.isSuccessful()) {
                    Post post = apiResponse.body.getData();
                    mDatabase.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            if (post.getLocation() != null)
                                mDatabase.getPostDao().insertLocation(post.getLocation());
                            mDatabase.getPostDao().insertFile(post.getFile());
                            mDatabase.getPostDao().insertPost(post);
                        }
                    });
                    mLiveData.postValue(Resource.success(true));
                } else {
                    if (apiResponse.isNotVerified())
                        mLiveData.postValue(Resource.logout(apiResponse.errorMessage, false));
                    else
                        mLiveData.postValue(Resource.error(apiResponse.errorMessage, false));
                }
            } catch (IOException e) {
                mLiveData.postValue(Resource.error(e.getMessage(), false));
            }
        }

        public MutableLiveData<Resource<Boolean>> getLiveData() {
            return mLiveData;
        }
    }

    public static class HidePost implements Runnable {
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final MiKDatabase mDatabase;
        private final PostResource mPostResource;
        private final String mPostsUrl;
        private final Post mPost;
        private final long uid;


        public HidePost(MiKDatabase database, PostResource postResource, String postsUrl, Post post, long uid) {
            mDatabase = database;
            mPostResource = postResource;
            mPostsUrl = postsUrl;
            mPost = post;
            this.uid = uid;
        }

        @Override
        public void run() {
            boolean existed = mDatabase.getPostDao().existsLoadResult(mPostsUrl);
            if (!existed) {
                mLiveData.postValue(null);
                return;
            }
            try {
                Response<ResponseUtil<Void>> response = mPostResource.hidePost(mPost.getId(), uid).execute();
                ApiResponse<ResponseUtil<Void>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    LoadResult postLoadResult = mDatabase.getPostDao().getPostLoadResult(Constants.Network.ENDPOINT_POSTS);
                    if (postLoadResult != null) {
                        if (postLoadResult.getIds().remove(mPost.getId())) {
                            postLoadResult.setTotal(postLoadResult.getTotal() - 1);
                        }
                    }

                    LoadResult groupPostLoadResult = mDatabase.getGroupDao().getLoadResult(mPost.getGroup()._posts());
                    if (groupPostLoadResult != null) {
                        if (groupPostLoadResult.getIds().remove(mPost.getId())) {
                            groupPostLoadResult.setTotal(groupPostLoadResult.getTotal() - 1);
                        }
                    }

                    mDatabase.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            mDatabase.getPostDao().deletePost(mPost);
                            if (postLoadResult != null) {
                                if (postLoadResult.getIds().isEmpty())
                                    mDatabase.getPostDao().deletePostLoadResult(postLoadResult);
                                else
                                    mDatabase.getPostDao().insertPostLoadResult(postLoadResult);
                            }

                            if (groupPostLoadResult != null) {
                                if (groupPostLoadResult.getIds().isEmpty())
                                    mDatabase.getPostDao().deletePostLoadResult(groupPostLoadResult);
                                else
                                    mDatabase.getPostDao().insertPostLoadResult(groupPostLoadResult);
                            }
                        }
                    });
                    mLiveData.postValue(Resource.success(true));
                } else {
                    if (apiResponse.isNotVerified()) {
                        mLiveData.postValue(Resource.logout(apiResponse.errorMessage, null));
                    } else {
                        mLiveData.postValue(Resource.error(apiResponse.errorMessage, null));
                    }
                }
            } catch (IOException e) {
                mLiveData.postValue(Resource.error(e.getMessage(), null));
            }
        }

        public MutableLiveData<Resource<Boolean>> getLiveData() {
            return mLiveData;
        }
    }

    public static class Refresh implements Runnable {
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final MiKDatabase mDatabase;
        private final PostResource mPostResource;
        private final String url;
        private static final String TAG = "Refresh";

        public Refresh(MiKDatabase database, PostResource postResource, String url) {
            mDatabase = database;
            mPostResource = postResource;
            this.url = url;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<PostLoadResponse>> response = mPostResource.posts(url).execute();
                ApiResponse<ResponseUtil<PostLoadResponse>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    PostLoadResponse data = apiResponse.body.getData();
                    if (data.getTotal() == 0)
                        return;
                    data.setNextPage(apiResponse.getNextPage());
                    List<Long> postIds = data.getIds();
                    LoadResult postLoadResult = new LoadResult(url, postIds, data.getNextPage(), data.getTotal());
                    mDatabase.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            for (Post post : data.getContents()) {

                                Location location = post.getLocation();
                                if (location != null)
                                    mDatabase.getPostDao().insertLocation(location);

                                Group group = post.getGroup();
                                mDatabase.getGroupDao().insertGroup(group);

                                Profile owner = post.getOwner();
                                mDatabase.getUserDao().insert(owner);

                                mDatabase.getPostDao().insertFile(post.getFile());

                                mDatabase.getPostDao().insertPost(post);
                            }
                            mDatabase.getPostDao().insertPostLoadResult(postLoadResult);
                        }
                    });
                    mLiveData.postValue(Resource.success(true));
                } else {
                    if (apiResponse.isNotVerified()) {
                        mLiveData.postValue(Resource.logout(apiResponse.errorMessage, false));
                    } else {
                        mLiveData.postValue(Resource.error(apiResponse.errorMessage, false));
                    }
                }
            } catch (IOException e) {
                mLiveData.postValue(Resource.error(e.getMessage(), null));
            }
        }

        public MutableLiveData<Resource<Boolean>> getLiveData() {
            return mLiveData;
        }
    }

    public static class FetchCommentNextPage implements Runnable {
        private final MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();
        private final String url;
        private final PostResource postResource;
        private final MiKDatabase database;
        private static final String TAG = "FetchCommentNextPage";

        public FetchCommentNextPage(String url, PostResource postResource, MiKDatabase database) {
            this.url = url;
            this.postResource = postResource;
            this.database = database;
        }

        @Override
        public void run() {
            LoadResult current = database.getPostDao().getCommentLoadResult(url);
            if (current == null) {
                liveData.postValue(null);
                return;
            }
            final Integer nextPage = current.getNext();
            if (nextPage == null) {
                liveData.postValue(Resource.success(false));
                return;
            }
            try {
                Response<ResponseUtil<CommentLoadResponse>> response = postResource.getComments(url, nextPage).execute();
                ApiResponse<ResponseUtil<CommentLoadResponse>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    List<Long> ids = new ArrayList<>();
                    ids.addAll(current.getIds());
                    ids.addAll(apiResponse.body.getData().getIds());
                    LoadResult merged = new LoadResult(url, ids, apiResponse.getNextPage(), apiResponse.body.getData().getTotal());
                    database.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            for (Comment comment : apiResponse.body.getData().getContents()) {
                                Profile owner = comment.getOwner();
                                database.getUserDao().insert(owner);
                                database.getPostDao().insertComment(comment);
                            }
                            database.getPostDao().insertCommentLoadResult(merged);
                        }
                    });
                    liveData.postValue(Resource.success(apiResponse.getNextPage() != null));
                } else {
                    if (apiResponse.isNotVerified())
                        liveData.postValue(Resource.logout(apiResponse.errorMessage, true));
                    else
                        liveData.postValue(Resource.error(apiResponse.errorMessage, true));
                }
            } catch (IOException e) {
                liveData.postValue(Resource.error(e.getMessage(), true));
            }
        }

        public MutableLiveData<Resource<Boolean>> getLiveData() {
            return liveData;
        }
    }

    public static class RefreshComments implements Runnable {
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final MiKDatabase mDatabase;
        private final PostResource mPostResource;
        private final String url;
        private static final String TAG = "RefreshComments";

        public RefreshComments(MiKDatabase database, PostResource postResource, String url) {
            mDatabase = database;
            mPostResource = postResource;
            this.url = url;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<CommentLoadResponse>> response = mPostResource.comments(url).execute();
                ApiResponse<ResponseUtil<CommentLoadResponse>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    CommentLoadResponse data = apiResponse.body.getData();
                    if (data.getTotal() == 0) {
                        return;
                    }
                    data.setNextPage(apiResponse.getNextPage());
                    List<Long> commentIds = data.getIds();
                    LoadResult commentLoadResult = new LoadResult(url, commentIds, data.getNextPage(), data.getTotal());
                    mDatabase.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            for (Comment comment : data.getContents()) {
                                Profile owner = comment.getOwner();
                                mDatabase.getUserDao().insert(owner);
                                mDatabase.getPostDao().insertComment(comment);
                            }
                            mDatabase.getPostDao().insertCommentLoadResult(commentLoadResult);
                        }
                    });
                    mLiveData.postValue(Resource.success(true));
                } else {
                    if (apiResponse.isNotVerified()) {
                        mLiveData.postValue(Resource.logout(apiResponse.errorMessage, false));
                    } else {
                        mLiveData.postValue(Resource.error(apiResponse.errorMessage, false));
                    }
                }
            } catch (IOException e) {
                mLiveData.postValue(Resource.error(e.getMessage(), null));
            }
        }

        public MutableLiveData<Resource<Boolean>> getLiveData() {
            return mLiveData;
        }
    }

    public static class AddComment implements Runnable {
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final MiKDatabase mDatabase;
        private final PostResource mPostResource;
        private final String url;
        private final Comment comment;

        public AddComment(MiKDatabase database, PostResource postResource, String url, Comment comment) {
            mDatabase = database;
            mPostResource = postResource;
            this.url = url;
            this.comment = comment;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<Comment>> response = mPostResource.saveComment(url, comment).execute();
                ApiResponse<ResponseUtil<Comment>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    Comment comment = apiResponse.body.getData();
                    LoadResult newResult = null;
                    LoadResult currentResult = mDatabase.getPostDao().getCommentLoadResult(url);
                    if (currentResult != null) {
                        List<Long> ids = new ArrayList<>();
                        ids.add(comment.getId());
                        ids.addAll(currentResult.getIds());
                        newResult = new LoadResult(url, ids, currentResult.getNext(), currentResult.getTotal() + 1);
                    } else {
                        newResult = new LoadResult(url, Collections.singletonList(comment.getId()), null, 1);
                    }
                    LoadResult finalNewResult = newResult;
                    mDatabase.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            mDatabase.getPostDao().insertComment(comment);
                            mDatabase.getPostDao().insertCommentLoadResult(finalNewResult);
                        }
                    });
                    mLiveData.postValue(Resource.success(true));
                } else {
                    if (apiResponse.isNotVerified()) {
                        mLiveData.postValue(Resource.logout(apiResponse.errorMessage, false));
                    } else {
                        mLiveData.postValue(Resource.error(apiResponse.errorMessage, false));
                    }
                }
            } catch (IOException e) {
                mLiveData.postValue(Resource.error(e.getMessage(), null));
            }
        }

        public MutableLiveData<Resource<Boolean>> getLiveData() {
            return mLiveData;
        }
    }

    public static class DeleteComment implements Runnable {
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final MiKDatabase mDatabase;
        private final PostResource mPostResource;
        private final String url;
        private final Comment comment;

        public DeleteComment(String url, Comment comment, MiKDatabase database, PostResource postResource) {
            this.comment = comment;
            this.url = url;
            this.mDatabase = database;
            this.mPostResource = postResource;
        }

        @Override
        public void run() {
            LoadResult commentLoadResult = mDatabase.getPostDao().getCommentLoadResult(url);
            if (commentLoadResult == null) {
                mLiveData.postValue(null);
                return;
            }
            try {
                Response<ResponseUtil<Void>> response = mPostResource.deleteComment(url, comment.getId()).execute();
                ApiResponse<ResponseUtil<Void>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    commentLoadResult.getIds().remove(comment.getId());
                    commentLoadResult.setTotal(commentLoadResult.getTotal() - 1);
                    mDatabase.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            mDatabase.getPostDao().deleteComment(comment);
                            if (commentLoadResult.getIds().isEmpty()) {
                                mDatabase.getPostDao().deleteCommentLoadResult(commentLoadResult);
                            } else {
                                mDatabase.getPostDao().insertCommentLoadResult(commentLoadResult);
                            }
                        }
                    });
                    mLiveData.postValue(Resource.success(true));
                } else {
                    if (apiResponse.isNotVerified())
                        mLiveData.postValue(Resource.logout(apiResponse.errorMessage, false));
                    else
                        mLiveData.postValue(Resource.error(apiResponse.errorMessage, false));
                }
            } catch (IOException e) {
                mLiveData.postValue(Resource.error(e.getMessage(), null));
            }
        }

        public MutableLiveData<Resource<Boolean>> getLiveData() {
            return mLiveData;
        }
    }
}