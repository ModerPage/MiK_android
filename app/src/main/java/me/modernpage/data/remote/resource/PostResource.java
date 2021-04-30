package me.modernpage.data.remote.resource;

import androidx.lifecycle.LiveData;

import me.modernpage.data.local.entity.Comment;
import me.modernpage.data.local.entity.Like;
import me.modernpage.data.local.entity.Location;
import me.modernpage.data.local.entity.Post;
import me.modernpage.data.remote.ApiResponse;
import me.modernpage.data.remote.model.response.CommentLoadResponse;
import me.modernpage.data.remote.model.response.LikeLoadResponse;
import me.modernpage.data.remote.model.response.PostLoadResponse;
import me.modernpage.util.ResponseUtil;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface PostResource {

    @Multipart
    @POST
    Call<ResponseUtil<Post>> savePost(@Url String url, @Part("post") Post post, @Part("file") MultipartBody file);

    @GET
    Call<Location> getLocation(@Url String url);

    @GET
    Call<Like> getLike(@Url String url, @Query("ownerId") long ownerId);

    @POST
    Call<ResponseUtil<Like>> saveLike(@Url String url, @Body Like like);

    @GET
    LiveData<ApiResponse<ResponseUtil<LikeLoadResponse>>> getLikes(@Url String url, @Query("ownerId") long ownerId);

    @GET
    LiveData<ApiResponse<ResponseUtil<CommentLoadResponse>>> getComments(@Url String url);

    @GET
    Call<ResponseUtil<CommentLoadResponse>> comments(@Url String url);

    @GET
    Call<ResponseUtil<CommentLoadResponse>> getComments(@Url String url, @Query("page") int page);

    @HTTP(method = "DELETE", hasBody = true)
    Call<Like> deleteLike(@Url String url, @Body Like like);

    @DELETE
    Call<ResponseUtil<Like>> deleteLike(@Url String url, @Query("ownerId") long ownerId);

    @DELETE
    Call<ResponseUtil<Void>> deletePost(@Url String url);

    @GET
    LiveData<ApiResponse<ResponseUtil<PostLoadResponse>>> getPosts(@Url String url);

    @GET
    Call<ResponseUtil<PostLoadResponse>> posts(@Url String url);

    @GET
    Call<ResponseUtil<PostLoadResponse>> getPosts(@Url String url, @Query("page") int page);

    @PUT
    Call<ResponseUtil<Post>> updatePost(@Url String postsUrl, @Body Post post);

    @GET("posts/{postId}")
    Call<ResponseUtil<Void>> hidePost(@Path("postId") long postId, @Query("userId") long userId);

    @POST
    Call<ResponseUtil<Comment>> saveComment(@Url String url, @Body Comment comment);

    @DELETE
    Call<ResponseUtil<Void>> deleteComment(@Url String url, @Query("commentId") long commentId);
}
