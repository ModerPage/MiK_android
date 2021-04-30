package me.modernpage.data.remote.resource;


import androidx.lifecycle.LiveData;

import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.remote.ApiResponse;
import me.modernpage.data.remote.model.AccountInfo;
import me.modernpage.data.remote.model.AuthResponse;
import me.modernpage.data.remote.model.response.FollowerLoadResponse;
import me.modernpage.data.remote.model.LoginRequest;
import me.modernpage.data.remote.model.PasswordRequest;
import me.modernpage.data.remote.model.RegisterRequest;
import me.modernpage.data.remote.model.response.UserLoadResponse;
import me.modernpage.util.ResponseUtil;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface UserResource {

    @PUT("settings/{profileId}")
    Call<ResponseUtil<Profile>> updateUser(@Path("profileId") long profileId, @Body AccountInfo accountInfo);

    @Multipart
    @POST("settings/{profileId}")
    Call<ResponseUtil<Profile>> updateUser(@Path("profileId") long profileId,
                                           @Part("accountInfo") AccountInfo accountInfo,
                                           @Part("file") MultipartBody file);

    @GET
    LiveData<ApiResponse<ResponseUtil<FollowerLoadResponse>>> getFollower(@Url String url, @Query("followerId") long uid);

    @POST("login")
    @Headers("No-Authentication: true")
    LiveData<ApiResponse<ResponseUtil<AuthResponse>>> loginLD(@Body LoginRequest loginRequest);

    @POST("login")
    @Headers("No-Authentication: true")
    Call<ResponseUtil<AuthResponse>> login(@Body LoginRequest loginRequest);

    @GET("forgetpassword/email")
    @Headers("No-Authentication: true")
    Call<ResponseUtil<Boolean>> checkEmail(@Query("email") String email);

    @PUT("forgetpassword/reset")
    @Headers("No-Authentication: true")
    Call<ResponseUtil<Boolean>> resetPassword(@Body PasswordRequest passwordRequest);

    @GET("logout")
    Call<ResponseUtil<Void>> logout();

    @POST("register")
    @Headers("No-Authentication: true")
    Call<ResponseUtil<Boolean>> register(@Body RegisterRequest registerRequest);

    @GET("users/{id}")
    LiveData<ApiResponse<ResponseUtil<Profile>>> getUserById(@Path("id") long id);

    @GET("settings/{profileId}")
    Call<ResponseUtil<AccountInfo>> getAccountInfoByProfileId(@Path("profileId") Long profileId);

    @POST("settings/password")
    Call<ResponseUtil<Boolean>> checkPassword(@Body PasswordRequest passwordRequest);

    @GET
    LiveData<ApiResponse<ResponseUtil<UserLoadResponse>>> getUsers(@Url String url);

    @GET
    Call<ResponseUtil<UserLoadResponse>> getUsers(@Url String url, @Query("page") int page);

    @POST
    Call<ResponseUtil<Void>> addRequest(@Url String url, @Query("requesterId") long uid);

    @DELETE
    Call<ResponseUtil<Void>> deleteRequest(@Url String url, @Query("requesterId") long uid);

    @DELETE
    Call<ResponseUtil<Void>> deleteFollower(@Url String url, @Query("followerId") long uid);

    @POST
    Call<ResponseUtil<Void>> addFollower(@Url String url, @Query("followerId") long uid);

    @GET
    Call<ResponseUtil<UserLoadResponse>> users(String url);
}
