package me.modernpage.data.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.modernpage.data.local.MiKDatabase;
import me.modernpage.data.local.dao.UserDao;
import me.modernpage.data.local.entity.AuthResult;
import me.modernpage.data.local.entity.FollowerLoadResult;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.remote.ApiResponse;
import me.modernpage.data.remote.model.AccountInfo;
import me.modernpage.data.remote.model.AuthResponse;
import me.modernpage.data.remote.model.LoginRequest;
import me.modernpage.data.remote.model.PasswordRequest;
import me.modernpage.data.remote.model.RegisterRequest;
import me.modernpage.data.remote.model.response.UserLoadResponse;
import me.modernpage.data.remote.resource.UserResource;
import me.modernpage.interceptor.AuthInterceptor;
import me.modernpage.util.RateLimiter;
import me.modernpage.util.Resource;
import me.modernpage.util.ResponseUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class UserTask {
    public static UpdateUser updateUser(long profileId, AccountInfo accountInfo, File avatar,
                                        UserResource userResource, MiKDatabase database) {
        return new UpdateUser(profileId, accountInfo, avatar, userResource, database);
    }

    public static CheckLogin checkLogin(String username,
                                        MiKDatabase database,
                                        AuthInterceptor authInterceptor,
                                        RateLimiter rateLimiter) {
        return new CheckLogin(username, database, authInterceptor, rateLimiter);
    }

    public static Login login(LoginRequest loginRequest,
                              UserResource userResource,
                              MiKDatabase database,
                              AuthInterceptor authInterceptor,
                              RateLimiter rateLimiter) {
        return new Login(loginRequest, userResource, database, authInterceptor, rateLimiter);
    }

    public static GetAccountInfo getAccountInfo(Long profileId, UserResource userResource) {
        return new GetAccountInfo(userResource, profileId);
    }

    public static CheckPassword checkPassword(PasswordRequest passwordRequest, UserResource userResource) {
        return new CheckPassword(passwordRequest, userResource);
    }

    public static Register register(UserResource userResource, RegisterRequest registerRequest) {
        return new Register(userResource, registerRequest);
    }

    public static Logout logout(MiKDatabase database, UserResource userResource, AuthInterceptor interceptor) {
        return new Logout(database, userResource, interceptor);
    }

    public static CheckEmail checkEmail(String email, UserResource userResource) {
        return new CheckEmail(email, userResource);
    }

    public static ResetPassword resetPassword(PasswordRequest passwordRequest,
                                              MiKDatabase database, UserResource resource) {
        return new ResetPassword(passwordRequest, database, resource);
    }

    public static CleanLogin cleanLogin(MiKDatabase database, AuthInterceptor authInterceptor) {
        return new CleanLogin(database, authInterceptor);
    }

    public static FollowControl followControl(String url, long profileId, long uid, UserResource userResource, MiKDatabase database) {
        return new FollowControl(database, userResource, uid, profileId, url);
    }

    public static FetchNextPage fetchNextPage(String url, UserResource userResource, MiKDatabase database) {
        return new FetchNextPage(url, userResource, database);
    }

    public static RefreshUsers refreshUsers(String url, UserResource userResource, MiKDatabase database) {
        return new RefreshUsers(url, userResource, database);
    }

    static class CheckLogin implements Runnable {
        private final MutableLiveData<Resource<Profile>> mLiveData = new MutableLiveData<>();
        private final String username;
        private final MiKDatabase mDatabase;
        private final AuthInterceptor mAuthInterceptor;
        private final RateLimiter mRateLimiter;

        public CheckLogin(String username,
                          MiKDatabase database,
                          AuthInterceptor authInterceptor,
                          RateLimiter rateLimiter) {
            this.username = username;
            mDatabase = database;
            mAuthInterceptor = authInterceptor;
            mRateLimiter = rateLimiter;
        }

        @Override
        public void run() {
            AuthResult authResult = mDatabase.getUserDao().getAuthResult(username);
            if (authResult == null || mRateLimiter.shouldFetch(username)) {
                mLiveData.postValue(null);
                return;
            }
            mAuthInterceptor.setToken(authResult.getToken());
            Profile profile = mDatabase.getUserDao().getProfileById(authResult.getProfileId());
            mLiveData.postValue(Resource.success(profile));
        }

        public MutableLiveData<Resource<Profile>> getLiveData() {
            return mLiveData;
        }
    }

    static class Login implements Runnable {
        private static final String TAG = "LoginTask";
        private final MutableLiveData<Resource<Profile>> mLiveData = new MutableLiveData<>();
        private final LoginRequest mLoginRequest;
        private final UserResource mUserResource;
        private final MiKDatabase mDatabase;
        private final AuthInterceptor mAuthInterceptor;
        private final RateLimiter mRateLimiter;

        public Login(LoginRequest loginRequest,
                     UserResource userResource,
                     MiKDatabase database,
                     AuthInterceptor authInterceptor,
                     RateLimiter rateLimiter) {
            mLoginRequest = loginRequest;
            mUserResource = userResource;
            mDatabase = database;
            mAuthInterceptor = authInterceptor;
            mRateLimiter = rateLimiter;
        }

        @Override
        public void run() {
            try {
                mRateLimiter.set(mLoginRequest.getUsername());
                Response<ResponseUtil<AuthResponse>> response = mUserResource.login(mLoginRequest).execute();
                ApiResponse<ResponseUtil<AuthResponse>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    ResponseUtil<AuthResponse> responseUtil = apiResponse.body;
                    AuthResponse authResponse = responseUtil.getData();
                    mDatabase.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            Profile profile = authResponse.getProfile();
                            UserDao userDao = mDatabase.getUserDao();
                            userDao.insert(profile);
                            userDao.insert(new AuthResult(authResponse.getToken(),
                                    mLoginRequest.getUsername(), profile.getId()));
                        }
                    });
                    mAuthInterceptor.setToken(authResponse.getToken());
                    mLiveData.postValue(Resource.success(authResponse.getProfile()));
                } else {
                    mRateLimiter.reset(mLoginRequest.getUsername());
                    mLiveData.postValue(Resource.error(apiResponse.errorMessage, null));
                }
            } catch (IOException e) {
                mRateLimiter.reset(mLoginRequest.getUsername());
                mLiveData.postValue(Resource.error(e.getMessage(), null));
            }
        }

        public MutableLiveData<Resource<Profile>> getLiveData() {
            return mLiveData;
        }
    }

    static class UpdateUser implements Runnable {
        private static final String TAG = "UpdateUser";
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final long profileId;
        private final AccountInfo mAccountInfo;
        private final File mAvatar;
        private final UserResource mUserResource;
        private final MiKDatabase mDatabase;

        UpdateUser(long profileId, AccountInfo accountInfo, File avatar,
                   UserResource userResource, MiKDatabase database) {
            this.profileId = profileId;
            mAccountInfo = accountInfo;
            mAvatar = avatar;
            mUserResource = userResource;
            mDatabase = database;
        }

        @Override
        public void run() {
            Call<ResponseUtil<Profile>> call;
            if (mAvatar != null) {
                RequestBody avatar = RequestBody.create(MultipartBody.FORM, mAvatar);
                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.addFormDataPart("file", "file", avatar);
                call = mUserResource.updateUser(profileId, mAccountInfo, builder.build());
            } else {
                call = mUserResource.updateUser(profileId, mAccountInfo);
            }
            try {
                Response<ResponseUtil<Profile>> response = call.execute();
                ApiResponse<ResponseUtil<Profile>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    ResponseUtil<Profile> profile = apiResponse.body;
                    Log.d(TAG, "run: updated profile: " + profile.getData());
                    mDatabase.getUserDao().insert(profile.getData());
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

    static class CheckEmail implements Runnable {
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final String email;
        private final UserResource mUserResource;

        public CheckEmail(String email, UserResource userResource) {
            this.email = email;
            mUserResource = userResource;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<Boolean>> response = mUserResource.checkEmail(email).execute();
                ApiResponse<ResponseUtil<Boolean>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    ResponseUtil<Boolean> result = apiResponse.body;
                    mLiveData.postValue(Resource.success(result.getData()));
                } else {
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

    static class ResetPassword implements Runnable {
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final PasswordRequest mPasswordRequest;
        private final MiKDatabase mDatabase;
        private final UserResource mUserResource;

        public ResetPassword(PasswordRequest passwordRequest,
                             MiKDatabase database, UserResource userResource) {
            mPasswordRequest = passwordRequest;
            mDatabase = database;
            mUserResource = userResource;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<Boolean>> response =
                        mUserResource.resetPassword(mPasswordRequest).execute();
                ApiResponse<ResponseUtil<Boolean>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    mDatabase.getUserDao().deleteAllAuthResult();
                    mLiveData.postValue(Resource.success(true));
                } else {
                    mLiveData.postValue(Resource.error(apiResponse.errorMessage, null));
                }
            } catch (IOException e) {
                mLiveData.postValue(Resource.error(e.getMessage(), null));
            }
        }

        public MutableLiveData<Resource<Boolean>> getLiveData() {
            return mLiveData;
        }
    }

    static class Logout implements Runnable {
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final MiKDatabase mDatabase;
        private final UserResource mUserResource;
        private final AuthInterceptor mInterceptor;
        private static final String TAG = "Logout";

        public Logout(MiKDatabase database, UserResource userResource, AuthInterceptor interceptor) {
            mDatabase = database;
            mUserResource = userResource;
            mInterceptor = interceptor;
        }


        @Override
        public void run() {
            try {
                Response<ResponseUtil<Void>> response = mUserResource.logout().execute();
                ApiResponse<ResponseUtil<Void>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    mDatabase.getUserDao().deleteAuthResult(mInterceptor.getToken());
                    mInterceptor.setToken(null);
                    mLiveData.postValue(Resource.success(true));
                } else {
                    Log.d(TAG, "run: apiResponse.isNotVerified(): " + apiResponse.isNotVerified() + ", code: " + apiResponse.code);
                    if (apiResponse.isNotVerified()) {
                        Log.d(TAG, "run: isnotverified: true");
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

    static class Register implements Runnable {
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final UserResource mUserResource;
        private final RegisterRequest mRegisterRequest;

        public Register(UserResource userResource, RegisterRequest registerRequest) {
            mUserResource = userResource;
            mRegisterRequest = registerRequest;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<Boolean>> response = mUserResource.register(mRegisterRequest).execute();
                ApiResponse<ResponseUtil<Boolean>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    ResponseUtil<Boolean> result = apiResponse.body;
                    mLiveData.postValue(Resource.success(result.getData()));
                } else {
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

    static class GetAccountInfo implements Runnable {
        private final MutableLiveData<Resource<AccountInfo>> mLiveData = new MutableLiveData<>();
        private final UserResource mUserResource;
        private final Long profileId;

        public GetAccountInfo(UserResource userResource, Long profileId) {
            mUserResource = userResource;
            this.profileId = profileId;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<AccountInfo>> response = mUserResource.getAccountInfoByProfileId(profileId).execute();
                ApiResponse<ResponseUtil<AccountInfo>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    mLiveData.postValue(Resource.success(apiResponse.body.getData()));
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

        public MutableLiveData<Resource<AccountInfo>> getLiveData() {
            return mLiveData;
        }
    }

    static class CheckPassword implements Runnable {
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final PasswordRequest mPasswordRequest;
        private final UserResource userResource;

        public CheckPassword(PasswordRequest passwordRequest, UserResource userResource) {
            mPasswordRequest = passwordRequest;
            this.userResource = userResource;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<Boolean>> response = userResource
                        .checkPassword(mPasswordRequest).execute();
                ApiResponse<ResponseUtil<Boolean>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    ResponseUtil<Boolean> result = apiResponse.body;
                    mLiveData.postValue(Resource.success(result.getData()));
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

    static class CleanLogin implements Runnable {
        private final MiKDatabase mDatabase;
        private final AuthInterceptor mAuthInterceptor;

        public CleanLogin(MiKDatabase database, AuthInterceptor authInterceptor) {
            mDatabase = database;
            mAuthInterceptor = authInterceptor;
        }

        @Override
        public void run() {
            mDatabase.getUserDao().deleteAllAuthResult();
            mAuthInterceptor.setToken(null);
        }
    }

    public static class FollowControl implements Runnable {
        private static final String TAG = "FollowControl";
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final MiKDatabase mDatabase;
        private final UserResource mUserResource;
        private final long uid;
        private final long profileId;
        private final String url;

        public FollowControl(MiKDatabase database, UserResource userResource, long uid, long profileId, String url) {
            mDatabase = database;
            mUserResource = userResource;
            this.uid = uid;
            this.profileId = profileId;
            this.url = url;
        }

        @Override
        public void run() {
            Profile currentUser = mDatabase.getUserDao().getProfileById(uid);
            LoadResult userLoadResult = mDatabase.getUserDao().getUserLoadResult(currentUser._following());
            FollowerLoadResult followerLoadResult = mDatabase.getUserDao().getFollowerLoadResult(url);
            if (followerLoadResult == null || (followerLoadResult.getFollowed() != null
                    && !followerLoadResult.getFollowed())) {
                // following
                try {
                    Response<ResponseUtil<Void>> response = mUserResource.addFollower(url, uid).execute();
                    ApiResponse<ResponseUtil<Void>> apiResponse = new ApiResponse<>(response);
                    if (apiResponse.isSuccessful()) {
                        if (followerLoadResult == null) {
                            followerLoadResult = new FollowerLoadResult(url, Collections.singletonList(uid), null, 1, true);
                        } else {
                            followerLoadResult.getIds().add(0, uid);
                            followerLoadResult.setTotal(followerLoadResult.getTotal() + 1);
                            followerLoadResult.setFollowed(true);
                        }

                        if (userLoadResult == null) {
                            userLoadResult = new LoadResult(currentUser._following(),
                                    Collections.singletonList(profileId), null, 1);
                        } else {
                            userLoadResult.getIds().add(0, profileId);
                            userLoadResult.setTotal(userLoadResult.getTotal() + 1);
                        }
                        LoadResult finalUserLoadResult = userLoadResult;
                        FollowerLoadResult finalFollowerLoadResult = followerLoadResult;
                        mDatabase.runInTransaction(new Runnable() {
                            @Override
                            public void run() {
                                mDatabase.getUserDao().insert(finalUserLoadResult);
                                mDatabase.getUserDao().insert(finalFollowerLoadResult);
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
            } else {
                // unfollow
                try {
                    Response<ResponseUtil<Void>> response = mUserResource.deleteFollower(url, uid).execute();
                    ApiResponse<ResponseUtil<Void>> apiResponse = new ApiResponse<>(response);
                    if (apiResponse.isSuccessful()) {
                        followerLoadResult.getIds().remove(uid);
                        followerLoadResult.setFollowed(false);
                        int followerTotal = followerLoadResult.getTotal();
                        followerLoadResult.setTotal(followerTotal > 0 ? followerTotal - 1 : followerTotal);

                        if (userLoadResult != null) {
                            userLoadResult.getIds().remove(profileId);
                            int followingTotal = userLoadResult.getTotal();
                            userLoadResult.setTotal(followingTotal > 0 ? followerTotal - 1 : followerTotal);
                        }

                        FollowerLoadResult finalFollowerLoadResult1 = followerLoadResult;
                        LoadResult finalUserLoadResult1 = userLoadResult;
                        mDatabase.runInTransaction(new Runnable() {
                            @Override
                            public void run() {
                                mDatabase.getUserDao().insert(finalFollowerLoadResult1);
                                if (finalUserLoadResult1 != null)
                                    mDatabase.getUserDao().insert(finalUserLoadResult1);
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
        }

        public MutableLiveData<Resource<Boolean>> getLiveData() {
            return mLiveData;
        }
    }

    public static class FetchNextPage implements Runnable {
        private final MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();
        private final String url;
        private final UserResource mUserResource;
        private final MiKDatabase mDatabase;

        public FetchNextPage(String url, UserResource userResource, MiKDatabase database) {
            this.url = url;
            mUserResource = userResource;
            mDatabase = database;
        }

        @Override
        public void run() {
            LoadResult current = mDatabase.getUserDao().getUserLoadResult(url);
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
                Response<ResponseUtil<UserLoadResponse>> response = mUserResource.getUsers(url, nextPage).execute();
                ApiResponse<ResponseUtil<UserLoadResponse>> apiResponse = new ApiResponse<ResponseUtil<UserLoadResponse>>(response);
                if (apiResponse.isSuccessful()) {
                    List<Long> ids = new ArrayList<>();
                    ids.addAll(current.getIds());
                    ids.addAll(apiResponse.body.getData().getIds());
                    LoadResult merged = new LoadResult(url, ids, apiResponse.getNextPage(),
                            apiResponse.body.getData().getTotal());
                    mDatabase.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            for (Profile profile : apiResponse.body.getData().getContents()) {
                                mDatabase.getUserDao().insert(profile);
                            }
                            mDatabase.getUserDao().insert(merged);
                        }
                    });
                    liveData.postValue(Resource.success(apiResponse.getNextPage() != null));
                } else {
                    if (apiResponse.isNotVerified())
                        liveData.postValue(Resource.logout(apiResponse.errorMessage, null));
                    else
                        liveData.postValue(Resource.error(apiResponse.errorMessage, null));
                }
            } catch (IOException e) {
                liveData.postValue(Resource.error(e.getMessage(), null));
            }
        }

        public MutableLiveData<Resource<Boolean>> getLiveData() {
            return liveData;
        }
    }

    public static class RefreshUsers implements Runnable {
        private final MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();
        private final String url;
        private final UserResource userResource;
        private final MiKDatabase database;

        public RefreshUsers(String url, UserResource userResource, MiKDatabase database) {
            this.url = url;
            this.userResource = userResource;
            this.database = database;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<UserLoadResponse>> response = userResource.users(url).execute();
                ApiResponse<ResponseUtil<UserLoadResponse>> apiResponse = new ApiResponse<ResponseUtil<UserLoadResponse>>(response);
                if (apiResponse.isSuccessful()) {
                    UserLoadResponse data = apiResponse.body.getData();
                    if (data.getTotal() == 0) {
                        return;
                    }
                    data.setNextPage(apiResponse.getNextPage());
                    List<Long> userIds = data.getIds();
                    LoadResult userLoadResult = new LoadResult(url, userIds, data.getNextPage(), data.getTotal());
                    database.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            for (Profile profile : apiResponse.body.getData().getContents()) {
                                database.getUserDao().insert(profile);
                            }
                            database.getUserDao().insert(userLoadResult);
                        }
                    });
                    liveData.postValue(Resource.success(true));
                } else {
                    if (apiResponse.isNotVerified()) {
                        liveData.postValue(Resource.logout(apiResponse.errorMessage, false));
                    } else {
                        liveData.postValue(Resource.error(apiResponse.errorMessage, false));
                    }
                }
            } catch (IOException e) {
                liveData.postValue(Resource.error(e.getMessage(), null));

            }
        }

        public MutableLiveData<Resource<Boolean>> getLiveData() {
            return liveData;
        }
    }
}
