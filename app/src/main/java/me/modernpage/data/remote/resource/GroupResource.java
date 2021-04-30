package me.modernpage.data.remote.resource;

import androidx.lifecycle.LiveData;

import java.util.List;

import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.data.local.entity.PublicGroup;
import me.modernpage.data.remote.ApiResponse;
import me.modernpage.data.remote.model.response.GroupLoadResponse;
import me.modernpage.data.remote.model.response.MemberLoadResponse;
import me.modernpage.util.ResponseUtil;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface GroupResource {
    @GET("publicgroups/")
    LiveData<ApiResponse<ResponseUtil<List<PublicGroup>>>> getPublicGroups();

    @GET
    LiveData<ApiResponse<ResponseUtil<GroupLoadResponse>>> getPrivateGroups(@Url String url);

    @GET
    Call<ResponseUtil<GroupLoadResponse>> getPrivateGroups(@Url String url, @Query("page") int page);

    @GET
    LiveData<ApiResponse<ResponseUtil<List<Group>>>> getGroupsByUrl(@Url String url);

    @GET
    Call<ResponseUtil<List<Group>>> getGroupsByUid(@Path("uid") long uid);

    @POST
    Call<ResponseUtil<PrivateGroup>> saveGroup(@Url String url, @Body PrivateGroup privateGroup);

    @Multipart
    @POST
    Call<ResponseUtil<PrivateGroup>> saveGroup(@Url String url, @Part("group") PrivateGroup privateGroup,
                                               @Part("file") MultipartBody file);

    @GET
    Call<ResponseUtil<GroupLoadResponse>> privateGroups(@Url String url);

    @GET
    LiveData<ApiResponse<ResponseUtil<MemberLoadResponse>>> getGroupMembers(@Url String url, @Query("userId") long uid);

    @DELETE
    Call<ResponseUtil<Void>> deleteGroup(@Url String self);

    @GET
    LiveData<ApiResponse<ResponseUtil<Boolean>>> checkJoin(@Url String url, @Query("userId") long uid);

    @POST
    Call<ResponseUtil<Void>> addMember(@Url String url, @Query("userId") long uid);

    @DELETE
    Call<ResponseUtil<Void>> leaveGroup(@Url String url, @Query("userId") long uid);

//    @GET
//    LiveData<ApiResponse<PostLoadResponse>> getGroupPosts(@Url String url);
//
//    @GET
//    Call<PostLoadResponse> getGroupPosts(@Url String url, @Query("page") int page);
}
