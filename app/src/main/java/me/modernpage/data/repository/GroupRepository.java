package me.modernpage.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import me.modernpage.data.local.MiKDatabase;
import me.modernpage.data.local.dao.GroupDao;
import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.MemberLoadResult;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.PublicGroup;
import me.modernpage.data.local.entity.model.LoadModel;
import me.modernpage.data.local.entity.model.Members;
import me.modernpage.data.remote.ApiResponse;
import me.modernpage.data.remote.model.response.GroupLoadResponse;
import me.modernpage.data.remote.model.response.MemberLoadResponse;
import me.modernpage.data.remote.resource.GroupResource;
import me.modernpage.util.AbsentLiveData;
import me.modernpage.util.AppExecutors;
import me.modernpage.util.Constants;
import me.modernpage.util.NetworkBoundResource;
import me.modernpage.util.Resource;
import me.modernpage.util.ResponseUtil;

public class GroupRepository {
    private final GroupDao mGroupDao;
    private final GroupResource mGroupResource;
    private final AppExecutors mAppExecutors;
    private final MiKDatabase mDatabase;


    @Inject
    public GroupRepository(GroupDao groupDao,
                           GroupResource groupResource,
                           AppExecutors appExecutors,
                           MiKDatabase database) {
        mGroupDao = groupDao;
        mAppExecutors = appExecutors;
        mGroupResource = groupResource;
        mDatabase = database;
    }

    public LiveData<Resource<List<PublicGroup>>> getPublicGroups() {
        return new NetworkBoundResource<List<PublicGroup>, ResponseUtil<List<PublicGroup>>>(mAppExecutors) {

            @Override
            protected void saveCallResult(@NonNull ResponseUtil<List<PublicGroup>> items) {
                mGroupDao.insertPublicGroups(items.getData());
            }

            @Override
            protected boolean shouldFetch(@Nullable List<PublicGroup> data) {
                if (data == null)
                    return true;
                else
                    return data.size() != 11;
            }

            @NonNull
            @Override
            protected LiveData<List<PublicGroup>> loadFromDb() {
                return mGroupDao.loadPublicGroups();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResponseUtil<List<PublicGroup>>>> createCall() {
                return mGroupResource.getPublicGroups();
            }
        }.asLiveData();
    }

    public LiveData<Resource<LoadModel<PrivateGroup>>> getPrivateGroups() {
        return new NetworkBoundResource<LoadModel<PrivateGroup>, ResponseUtil<GroupLoadResponse>>(mAppExecutors) {

            @Override
            protected void saveCallResult(@NonNull ResponseUtil<GroupLoadResponse> item) {
                GroupLoadResponse groupLoadResponse = item.getData();
                if (groupLoadResponse.getTotal() == 0) {
                    return;
                }
                List<Long> groupIds = groupLoadResponse.getIds();
                LoadResult loadResult = new LoadResult(Constants.Network.ENDPOINT_PRIVATE_GROUPS, groupIds,
                        groupLoadResponse.getNextPage(), groupLoadResponse.getTotal());
                mDatabase.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        for (PrivateGroup privateGroup : groupLoadResponse.getContents()) {
                            mDatabase.getGroupDao().insertPrivateGroup(privateGroup);
                        }
                        mDatabase.getGroupDao().insert(loadResult);
                    }
                });
            }

            @Override
            protected boolean shouldFetch(@Nullable LoadModel<PrivateGroup> data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<LoadModel<PrivateGroup>> loadFromDb() {
                return Transformations.switchMap(mGroupDao.getLoadResultLD(Constants.Network.ENDPOINT_PRIVATE_GROUPS), input -> {
                    if (input == null)
                        return AbsentLiveData.create();
                    return Transformations.switchMap(mGroupDao.loadOrderedPrivateGroups(input.getIds()), result -> {
                        if (result == null)
                            return AbsentLiveData.create();
                        return new MutableLiveData<>(new LoadModel<>(result, input.getTotal()));
                    });
                });
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResponseUtil<GroupLoadResponse>>> createCall() {
                return mGroupResource.getPrivateGroups(Constants.Network.ENDPOINT_PRIVATE_GROUPS);
            }

            @Override
            protected ResponseUtil<GroupLoadResponse> processResponse(ApiResponse<ResponseUtil<GroupLoadResponse>> response) {
                ResponseUtil<GroupLoadResponse> body = response.body;
                if (body != null)
                    body.getData().setNextPage(response.getNextPage());
                return body;
            }
        }.asLiveData();
    }


    public LiveData<Resource<LoadModel<Group>>> getUserGroups(String url) {
        return new NetworkBoundResource<LoadModel<Group>, ResponseUtil<List<Group>>>(mAppExecutors) {

            @Override
            protected void saveCallResult(@NonNull ResponseUtil<List<Group>> item) {
                List<Group> groups = item.getData();
                List<Long> groupIds = new ArrayList<>();
                for (Group group : groups) {
                    groupIds.add(group.getId());
                }
                LoadResult loadResult = new LoadResult(url, groupIds, null, groups.size());
                mDatabase.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        for (Group group : groups) {
                            mDatabase.getGroupDao().insert(group);
                        }
                        mDatabase.getGroupDao().insert(loadResult);
                    }
                });
            }

            @Override
            protected boolean shouldFetch(@Nullable LoadModel<Group> data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<LoadModel<Group>> loadFromDb() {
                return Transformations.switchMap(mGroupDao.getLoadResultLD(url), input -> {
                    if (input == null)
                        return AbsentLiveData.create();
                    return Transformations.switchMap(mGroupDao.loadOrderedGroups(input.getIds()), result -> {
                        if (result == null)
                            return AbsentLiveData.create();
                        return new MutableLiveData<>(new LoadModel<Group>(result, input.getTotal()));
                    });
                });
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResponseUtil<List<Group>>>> createCall() {
                return mGroupResource.getGroupsByUrl(url);
            }
        }.asLiveData();
    }

//    public LiveData<Resource<List<Group>>> getGroupsByUid(long uid) {
//        GroupTask.LoadUserGroups loadUserGroups = GroupTask.loadUserGroups(uid, mGroupResource);
//        mAppExecutors.networkIO().execute(loadUserGroups);
//        return loadUserGroups.getLiveData();
//    }

    public LiveData<Resource<Boolean>> saveGroup(PrivateGroup privateGroup, File groupImage) {
        GroupTask.CreateGroup createGroup = GroupTask.createGroup(privateGroup, groupImage, mGroupResource, mDatabase);
        mAppExecutors.networkIO().execute(createGroup);
        return createGroup.getLiveData();
    }

    public LiveData<Resource<Boolean>> loadGroupsNextPage(String url) {
        GroupTask.LoadNextPage loadNextPage = GroupTask.loadNextPage(url, mGroupResource, mDatabase);
        mAppExecutors.networkIO().execute(loadNextPage);
        return loadNextPage.getLiveData();
    }

    public LiveData<Resource<Boolean>> refreshGroups(String url) {
        GroupTask.Refresh refresh = GroupTask.refreshPosts(url, mGroupResource, mDatabase);
        mAppExecutors.networkIO().execute(refresh);
        return refresh.getLiveData();
    }

    public LiveData<Resource<Members>> getGroupMembers(String url, long uid) {
        return new NetworkBoundResource<Members, ResponseUtil<MemberLoadResponse>>(mAppExecutors) {

            @Override
            protected void saveCallResult(@NonNull ResponseUtil<MemberLoadResponse> item) {
                MemberLoadResponse data = item.getData();
                if (data.getTotal() == 0)
                    return;
                List<Long> userIds = data.getIds();
                MemberLoadResult loadResult = new MemberLoadResult(url, userIds, data.getNextPage(), data.getTotal(), data.getJoined());
                mDatabase.runInTransaction(new Runnable() {
                    @Override
                    public void run() {
                        for (Profile profile : data.getContents()) {
                            mDatabase.getUserDao().insert(profile);
                        }
                        mDatabase.getUserDao().insert(loadResult);
                    }
                });
            }

            @Override
            protected boolean shouldFetch(@Nullable Members data) {
                return data == null;
            }

            @NonNull
            @Override
            protected LiveData<Members> loadFromDb() {
                return Transformations.switchMap(mDatabase.getUserDao().getMemberLoadResultLD(url), input -> {
                    if (input == null)
                        return AbsentLiveData.create();
                    return Transformations.switchMap(mDatabase.getUserDao().loadOrderedUsers(input.getIds()), result -> {
                        if (result == null)
                            return AbsentLiveData.create();
                        return new MutableLiveData<>(new Members(result, input.getTotal(), input.joined));
                    });
                });
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<ResponseUtil<MemberLoadResponse>>> createCall() {
                return mGroupResource.getGroupMembers(url, uid);
            }

            @Override
            protected ResponseUtil<MemberLoadResponse> processResponse(ApiResponse<ResponseUtil<MemberLoadResponse>> response) {
                ResponseUtil<MemberLoadResponse> body = response.body;
                if (body != null)
                    body.getData().setNextPage(response.getNextPage());
                return body;
            }
        }.asLiveData();
    }


    public LiveData<Resource<Boolean>> deleteGroup(PrivateGroup group) {
        GroupTask.Delete deleteGroup = GroupTask.delete(group, mGroupResource, mDatabase);
        mAppExecutors.networkIO().execute(deleteGroup);
        return deleteGroup.getLiveData();
    }


    public LiveData<Resource<Boolean>> addMember(String url, long groupId, long uid) {
        GroupTask.AddMember addMember = GroupTask.addMember(url, groupId, uid, mGroupResource, mDatabase);
        mAppExecutors.networkIO().execute(addMember);
        return addMember.getLiveData();
    }

    public LiveData<Resource<Boolean>> leaveGroup(String url, long groupId, long uid) {
        GroupTask.LeaveGroup leaveGroup = GroupTask.leaveGroup(url, groupId, uid, mGroupResource, mDatabase);
        mAppExecutors.networkIO().execute(leaveGroup);
        return leaveGroup.getLiveData();
    }
}
