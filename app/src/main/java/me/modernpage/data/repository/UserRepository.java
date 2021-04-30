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
import me.modernpage.data.local.dao.UserDao;
import me.modernpage.data.local.entity.FollowerLoadResult;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.model.Followers;
import me.modernpage.data.local.entity.model.LoadModel;
import me.modernpage.data.remote.ApiResponse;
import me.modernpage.data.remote.model.AccountInfo;
import me.modernpage.data.remote.model.response.FollowerLoadResponse;
import me.modernpage.data.remote.model.LoginRequest;
import me.modernpage.data.remote.model.PasswordRequest;
import me.modernpage.data.remote.model.RegisterRequest;
import me.modernpage.data.remote.model.response.UserLoadResponse;
import me.modernpage.data.remote.resource.UserResource;
import me.modernpage.interceptor.AuthInterceptor;
import me.modernpage.util.AbsentLiveData;
import me.modernpage.util.AppExecutors;
import me.modernpage.util.NetworkBoundResource;
import me.modernpage.util.RateLimiter;
import me.modernpage.util.Resource;
import me.modernpage.util.ResponseUtil;

public class UserRepository {
    private static final String TAG = "UserRepository";
    private final UserDao mUserDao;
    private final RateLimiter mRateLimiter;
    private final UserResource mUserResource;
    private final AppExecutors mAppExecutors;
    private final MiKDatabase mDatabase;
    private final AuthInterceptor mAuthInterceptor;

    @Inject
    public UserRepository(UserDao userDao,
                          UserResource userResource,
                          AuthInterceptor authInterceptor,
                          AppExecutors appExecutors,
                          MiKDatabase database,
                          RateLimiter rateLimiter) {
        mAppExecutors = appExecutors;
        mDatabase = database;
        mUserDao = userDao;
        mUserResource = userResource;
        mAuthInterceptor = authInterceptor;
        mRateLimiter = rateLimiter;
    }

    public LiveData<Resource<Profile>> getUserById(long profileId) {
        return new NetworkBoundResource<Profile, ResponseUtil<Profile>>(mAppExecutors) {
            @Override
            protected void saveCallResult(@NonNull ResponseUtil<Profile> item) {
                mUserDao.insert(item.getData());
            }

            @Override
            protected boolean shouldFetch(@Nullable Profile data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<Profile> loadFromDb() {
                return Transformations.distinctUntilChanged(mUserDao.getProfileLDById(profileId));
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResponseUtil<Profile>>> createCall() {
                return mUserResource.getUserById(profileId);
            }

        }.asLiveData();
    }


    public LiveData<Resource<Boolean>> update(long profileId, AccountInfo accountInfo, File avatar) {
        Log.d(TAG, "update: called");
        UserTask.UpdateUser updateUser = UserTask.updateUser(profileId, accountInfo, avatar, mUserResource, mDatabase);
        mAppExecutors.networkIO().execute(updateUser);
        return updateUser.getLiveData();
    }

    public LiveData<Resource<Profile>> loginProcess(LoginRequest loginRequest) {
        UserTask.Login login = UserTask.login(loginRequest, mUserResource, mDatabase, mAuthInterceptor, mRateLimiter);
        mAppExecutors.networkIO().execute(login);
        return login.getLiveData();
    }

    public LiveData<Resource<Profile>> checkAuth(String username) {
        UserTask.CheckLogin checkLogin = UserTask.checkLogin(
                username,
                mDatabase,
                mAuthInterceptor,
                mRateLimiter);
        mAppExecutors.networkIO().execute(checkLogin);
        return checkLogin.getLiveData();
    }

    public LiveData<Resource<Boolean>> checkEmail(String email) {
        UserTask.CheckEmail checkEmail = UserTask.checkEmail(
                email,
                mUserResource);
        mAppExecutors.networkIO().execute(checkEmail);
        return checkEmail.getLiveData();
    }

    public LiveData<Resource<Boolean>> resetPassword(PasswordRequest passwordRequest) {
        UserTask.ResetPassword resetPassword = UserTask.resetPassword(
                passwordRequest,
                mDatabase,
                mUserResource);
        mAppExecutors.networkIO().execute(resetPassword);
        return resetPassword.getLiveData();
    }

    public LiveData<Resource<Boolean>> logout() {
        UserTask.Logout logout = UserTask.logout(
                mDatabase,
                mUserResource,
                mAuthInterceptor
        );
        mAppExecutors.networkIO().execute(logout);
        return logout.getLiveData();
    }

    public void relogin() {
        UserTask.CleanLogin cleanLogin = UserTask.cleanLogin(mDatabase, mAuthInterceptor);
        mAppExecutors.networkIO().execute(cleanLogin);
    }

    public LiveData<Resource<Boolean>> register(RegisterRequest registerRequest) {
        UserTask.Register register = UserTask.register(
                mUserResource,
                registerRequest);

        mAppExecutors.networkIO().execute(register);
        return register.getLiveData();
    }

    public LiveData<Resource<AccountInfo>> getAccountInfoByProfileId(Long profileId) {
        UserTask.GetAccountInfo getAccountInfo = UserTask.getAccountInfo(
                profileId,
                mUserResource);
        mAppExecutors.networkIO().execute(getAccountInfo);
        return getAccountInfo.getLiveData();
    }

    public LiveData<Resource<Boolean>> checkPassword(PasswordRequest passwordRequest) {
        UserTask.CheckPassword checkPassword = UserTask.checkPassword(passwordRequest, mUserResource);
        mAppExecutors.networkIO().execute(checkPassword);
        return checkPassword.getLiveData();
    }

    public LiveData<Resource<LoadModel<Profile>>> getUsers(String url) {
        return new NetworkBoundResource<LoadModel<Profile>, ResponseUtil<UserLoadResponse>>(mAppExecutors) {

            @Override
            protected void saveCallResult(@NonNull ResponseUtil<UserLoadResponse> item) {
                UserLoadResponse response = item.getData();
                if (response.getTotal() == 0)
                    return;
                List<Long> userIds = response.getIds();
                LoadResult userLoadResult = new LoadResult(url, userIds, response.getNextPage(), response.getTotal());
                mDatabase.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        for (Profile profile : response.getContents()) {
                            mUserDao.insert(profile);
                        }
                        mUserDao.insert(userLoadResult);
                    }
                });
            }

            @Override
            protected boolean shouldFetch(@Nullable LoadModel<Profile> data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<LoadModel<Profile>> loadFromDb() {
                return Transformations.switchMap(mUserDao.getUserLoadResultLD(url), input -> {
                    if (input == null)
                        return AbsentLiveData.create();
                    return Transformations.switchMap(mUserDao.loadOrderedUsers(input.getIds()), result -> {
                        if (result == null)
                            return AbsentLiveData.create();
                        return new MutableLiveData<>(new LoadModel<>(result, input.getTotal()));
                    });
                });
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResponseUtil<UserLoadResponse>>> createCall() {
                return mUserResource.getUsers(url);
            }

            @Override
            protected ResponseUtil<UserLoadResponse> processResponse(ApiResponse<ResponseUtil<UserLoadResponse>> response) {
                ResponseUtil<UserLoadResponse> body = response.body;
                if (body != null)
                    body.getData().setNextPage(response.getNextPage());
                return body;
            }
        }.asLiveData();
    }

    public LiveData<Resource<Followers>> getFollowers(String url, long uid) {
        return new NetworkBoundResource<Followers, ResponseUtil<FollowerLoadResponse>>(mAppExecutors) {

            @Override
            protected void saveCallResult(@NonNull ResponseUtil<FollowerLoadResponse> item) {
                Log.d(TAG, "saveCallResult: called: " + item);
                FollowerLoadResponse data = item.getData();
                mDatabase.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        for (Profile profile : data.getContents()) {
                            mDatabase.getUserDao().insert(profile);
                        }
                        FollowerLoadResult followerLoadResult = new FollowerLoadResult(url, data.getIds(), data.getNextPage(), data.getTotal(), data.getFollowed());
                        mDatabase.getUserDao().insert(followerLoadResult);
                    }
                });
            }

            @Override
            protected boolean shouldFetch(@Nullable Followers data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<Followers> loadFromDb() {
                return Transformations.switchMap(mUserDao.getFollowerLoadResultLD(url), input -> {
                    if (input == null)
                        return AbsentLiveData.create();
                    return Transformations.switchMap(mUserDao.loadOrderedUsers(input.getIds()), result -> {
                        if (result == null)
                            return AbsentLiveData.create();
                        return new MutableLiveData<>(new Followers(result, input.getTotal(), input.getFollowed()));
                    });
                });
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResponseUtil<FollowerLoadResponse>>> createCall() {
                Log.d(TAG, "createCall: called");
                return mUserResource.getFollower(url, uid);
            }

            @Override
            protected ResponseUtil<FollowerLoadResponse> processResponse(ApiResponse<ResponseUtil<FollowerLoadResponse>> response) {
                Log.d(TAG, "processResponse: called: " + response.body);
                ResponseUtil<FollowerLoadResponse> body = response.body;
                if (body != null)
                    body.getData().setNextPage(response.getNextPage());
                return body;
            }
        }.asLiveData();
    }

    public LiveData<Resource<Boolean>> followControl(String url, long profileId, long uid) {
        UserTask.FollowControl followControl = UserTask.followControl(url, profileId, uid, mUserResource, mDatabase);
        mAppExecutors.networkIO().execute(followControl);
        return followControl.getLiveData();
    }

    public LiveData<Resource<Boolean>> loadUsersNextPage(String url) {
        UserTask.FetchNextPage fetchNextPage = UserTask.fetchNextPage(url, mUserResource, mDatabase);
        mAppExecutors.networkIO().execute(fetchNextPage);
        return fetchNextPage.getLiveData();
    }

    public LiveData<Resource<Boolean>> refreshUsers(String url) {
        UserTask.RefreshUsers refreshUsers = UserTask.refreshUsers(url, mUserResource, mDatabase);
        mAppExecutors.networkIO().execute(refreshUsers);
        return refreshUsers.getLiveData();
    }

//    public LiveData<Resource<Followers<Profile>>> getFollowers(String url, long uid, long followerId) {
//        UserTask.GetFollower getFollower = UserTask.getFollower(mUserResource, mDatabase, url, uid, followerId);
//        mAppExecutors.networkIO().execute(getFollower);
//        return getFollower.getLiveData();
//    }
}
