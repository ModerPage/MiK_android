package me.modernpage.data.repository;

import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.modernpage.data.local.MiKDatabase;
import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.MemberLoadResult;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.remote.ApiResponse;
import me.modernpage.data.remote.model.response.GroupLoadResponse;
import me.modernpage.data.remote.resource.GroupResource;
import me.modernpage.util.Constants;
import me.modernpage.util.Resource;
import me.modernpage.util.ResponseUtil;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class GroupTask {

    public static LoadUserGroups loadUserGroups(long uid, GroupResource groupResource) {
        return new LoadUserGroups(uid, groupResource);
    }

    public static CreateGroup createGroup(PrivateGroup privateGroup, File groupImage, GroupResource groupResource, MiKDatabase database) {
        return new CreateGroup(privateGroup, groupImage, groupResource, database);
    }

    public static LoadNextPage loadNextPage(String url, GroupResource groupResource, MiKDatabase database) {
        return new LoadNextPage(url, groupResource, database);
    }

    public static Refresh refreshPosts(String url, GroupResource groupResource, MiKDatabase database) {
        return new Refresh(url, groupResource, database);
    }

    public static Delete delete(PrivateGroup group, GroupResource groupResource, MiKDatabase database) {
        return new Delete(group, groupResource, database);
    }


    public static AddMember addMember(String url, long groupId, long uid, GroupResource groupResource, MiKDatabase database) {
        return new AddMember(database, groupResource, url, groupId, uid);
    }

    public static LeaveGroup leaveGroup(String url, long groupId, long uid, GroupResource groupResource, MiKDatabase database) {
        return new LeaveGroup(groupResource, database, url, uid, groupId);
    }

    static class LoadUserGroups implements Runnable {
        private static final String TAG = "LoadUserGroups";
        private final MutableLiveData<Resource<List<Group>>> mLiveData = new MutableLiveData<>();
        private final long uid;
        private final GroupResource groupResource;

        public LoadUserGroups(long uid, GroupResource groupResource) {
            this.uid = uid;
            this.groupResource = groupResource;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<List<Group>>> response = groupResource.getGroupsByUid(uid).execute();
                ApiResponse<ResponseUtil<List<Group>>> apiResponse = new ApiResponse<>(response);
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
                e.printStackTrace();
            }
        }

        public MutableLiveData<Resource<List<Group>>> getLiveData() {
            return mLiveData;
        }
    }

    public static class CreateGroup implements Runnable {
        private static final String TAG = "CreateGroup";
        private final MutableLiveData<Resource<Boolean>> mLiveData = new MutableLiveData<>();
        private final PrivateGroup privateGroup;
        private final File groupImage;
        private final GroupResource groupResource;
        private final MiKDatabase database;

        public CreateGroup(PrivateGroup privateGroup, File groupImage, GroupResource groupResource, MiKDatabase database) {
            this.privateGroup = privateGroup;
            this.groupImage = groupImage;
            this.groupResource = groupResource;
            this.database = database;
        }

        @Override
        public void run() {
            Call<ResponseUtil<PrivateGroup>> call;
            if (groupImage == null) {
                call = groupResource.saveGroup(privateGroup.getOwner()._groups(), privateGroup);
            } else {
                RequestBody groupFile = RequestBody.create(groupImage, MultipartBody.FORM);
                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.addFormDataPart("file", "file", groupFile);
                call = groupResource.saveGroup(privateGroup.getOwner()._groups(), privateGroup, builder.build());
            }
            try {
                Response<ResponseUtil<PrivateGroup>> response = call.execute();
                ApiResponse<ResponseUtil<PrivateGroup>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    PrivateGroup data = apiResponse.body.getData();
                    LoadResult loadResult = database.getGroupDao().getLoadResult(Constants.Network.ENDPOINT_PRIVATE_GROUPS);
                    if (loadResult == null) {
                        loadResult = new LoadResult(Constants.Network.ENDPOINT_PRIVATE_GROUPS, Collections.singletonList(data.getId()), null, 1);
                    } else {
                        loadResult.getIds().add(data.getId());
                        loadResult.setTotal(loadResult.getTotal() + 1);
                    }
                    LoadResult finalLoadResult = loadResult;
                    database.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            database.getGroupDao().insert(finalLoadResult);
                            database.getGroupDao().insertPrivateGroup(data);
                            database.getGroupDao().insertGroup(data);
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

    public static class LoadNextPage implements Runnable {
        private final MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();
        private final String url;
        private final GroupResource groupResource;
        private final MiKDatabase database;

        public LoadNextPage(String url, GroupResource groupResource, MiKDatabase database) {
            this.url = url;
            this.groupResource = groupResource;
            this.database = database;
        }

        @Override
        public void run() {
            LoadResult current = database.getGroupDao().getLoadResult(url);
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
                Response<ResponseUtil<GroupLoadResponse>> response = groupResource.getPrivateGroups(url, nextPage).execute();
                ApiResponse<ResponseUtil<GroupLoadResponse>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    List<Long> ids = new ArrayList<>();
                    ids.addAll(current.getIds());
                    ids.addAll(apiResponse.body.getData().getIds());
                    LoadResult merged = new LoadResult(url, ids, apiResponse.getNextPage(),
                            apiResponse.body.getData().getTotal());
                    database.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            for (PrivateGroup group : apiResponse.body.getData().getContents()) {
                                database.getGroupDao().insert(group);
                            }
                            database.getGroupDao().insert(merged);
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

    public static class Refresh implements Runnable {
        private final MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();
        private final String url;
        private final GroupResource groupResource;
        private final MiKDatabase database;

        public Refresh(String url, GroupResource groupResource, MiKDatabase database) {
            this.url = url;
            this.groupResource = groupResource;
            this.database = database;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<GroupLoadResponse>> response = groupResource.privateGroups(url).execute();
                ApiResponse<ResponseUtil<GroupLoadResponse>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    GroupLoadResponse data = apiResponse.body.getData();
                    if (data.getTotal() == 0) {
                        return;
                    }
                    data.setNextPage(apiResponse.getNextPage());
                    List<Long> groupIds = data.getIds();
                    LoadResult groupLoadResult = new LoadResult(url, groupIds, data.getNextPage(), data.getTotal());
                    database.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            for (PrivateGroup group : data.getContents()) {
                                database.getGroupDao().insert(group);
                            }
                            database.getGroupDao().insert(groupLoadResult);
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

    public static class Delete implements Runnable {
        private final MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();
        private final PrivateGroup privateGroup;
        private final GroupResource groupResource;
        private final MiKDatabase database;

        public Delete(PrivateGroup privateGroup, GroupResource groupResource, MiKDatabase database) {
            this.privateGroup = privateGroup;
            this.groupResource = groupResource;
            this.database = database;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<Void>> response = groupResource.deleteGroup(privateGroup._self()).execute();
                ApiResponse<ResponseUtil<Void>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    LoadResult homePostsLoad = database.getPostDao().getPostLoadResult(Constants.Network.ENDPOINT_POSTS);
                    LoadResult groupLoadResult = database.getGroupDao()
                            .getLoadResult(Constants.Network.ENDPOINT_PRIVATE_GROUPS);
                    MemberLoadResult membersLoadResult = database.getUserDao().getMemberLoadResult(privateGroup._members());
                    LoadResult postsLoadResult = database.getPostDao().getPostLoadResult(privateGroup._posts());
                    groupLoadResult.getIds().remove(privateGroup.getId());
                    groupLoadResult.setTotal(groupLoadResult.getTotal() - 1);
                    if (postsLoadResult != null) {
                        homePostsLoad.getIds().removeAll(postsLoadResult.getIds());
                        homePostsLoad.setTotal(homePostsLoad.getTotal() - postsLoadResult.getTotal());
                    }

                    Profile profile = database.getUserDao().getProfileById(privateGroup.getOwnerId());
                    LoadResult userGroupResult = database.getGroupDao().getLoadResult(profile._groups());
                    if (userGroupResult != null) {
                        userGroupResult.getIds().remove(privateGroup.getId());
                        userGroupResult.setTotal(userGroupResult.getTotal() - 1);
                    }
                    database.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            database.getUserDao().deleteLoadResult(membersLoadResult);
                            if (postsLoadResult != null) {
                                database.getPostDao().deletePostLoadResult(postsLoadResult);
                                database.getPostDao().deletePosts(postsLoadResult.getIds());
                                database.getPostDao().insertPostLoadResult(homePostsLoad);
                            }
                            database.getGroupDao().delete(privateGroup);
                            database.getGroupDao().delete((Group) privateGroup);
                            database.getGroupDao().insert(groupLoadResult);

                            if (userGroupResult != null)
                                database.getGroupDao().insert(userGroupResult);
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


    public static class AddMember implements Runnable {
        private final MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();
        private final MiKDatabase database;
        private final GroupResource groupResource;
        private final String url;
        private final long groupId;
        private final long uid;

        public AddMember(MiKDatabase database, GroupResource groupResource, String url, long groupId, long uid) {
            this.database = database;
            this.groupResource = groupResource;
            this.url = url;
            this.groupId = groupId;
            this.uid = uid;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<Void>> response = groupResource.addMember(url, uid).execute();
                ApiResponse<ResponseUtil<Void>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    Profile profile = database.getUserDao().getProfileById(uid);
                    LoadResult userGroupResult = database.getGroupDao().getLoadResult(profile._groups());
                    if (userGroupResult != null) {
                        userGroupResult.getIds().add(groupId);
                        userGroupResult.setTotal(userGroupResult.getTotal() + 1);
                    }
                    MemberLoadResult memberLoadResult = database.getUserDao().getMemberLoadResult(url);
                    memberLoadResult.getIds().add(uid);
                    memberLoadResult.setTotal(memberLoadResult.getTotal() + 1);
                    memberLoadResult.setJoined(true);
                    database.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            if (userGroupResult != null)
                                database.getGroupDao().insert(userGroupResult);
                            database.getUserDao().insert(memberLoadResult);
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

    public static class LeaveGroup implements Runnable {
        private final MutableLiveData<Resource<Boolean>> liveData = new MutableLiveData<>();
        private final GroupResource groupResource;
        private final MiKDatabase database;
        private final String url;
        private final long uid;
        private final long groupId;

        public LeaveGroup(GroupResource groupResource, MiKDatabase database, String url, long uid, long groupId) {
            this.groupResource = groupResource;
            this.database = database;
            this.url = url;
            this.uid = uid;
            this.groupId = groupId;
        }

        @Override
        public void run() {
            try {
                Response<ResponseUtil<Void>> response = groupResource.leaveGroup(url, uid).execute();
                ApiResponse<ResponseUtil<Void>> apiResponse = new ApiResponse<>(response);
                if (apiResponse.isSuccessful()) {
                    Profile profile = database.getUserDao().getProfileById(uid);
                    LoadResult userGroupLoadResult = database.getGroupDao().getLoadResult(profile._groups());
                    if (userGroupLoadResult != null) {
                        userGroupLoadResult.getIds().remove(groupId);
                        userGroupLoadResult.setTotal(userGroupLoadResult.getTotal() - 1);
                    }

                    MemberLoadResult memberLoadResult = database.getUserDao().getMemberLoadResult(url);
                    memberLoadResult.getIds().remove(uid);
                    memberLoadResult.setTotal(memberLoadResult.getTotal() - 1);
                    memberLoadResult.setJoined(false);

                    database.runInTransaction(new Runnable() {
                        @Override
                        public void run() {
                            if (userGroupLoadResult != null)
                                database.getGroupDao().insert(userGroupLoadResult);
                            database.getUserDao().insert(memberLoadResult);
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
