package me.modernpage.ui.addedit;

import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.io.File;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.model.LoadModel;
import me.modernpage.data.repository.GroupRepository;
import me.modernpage.data.repository.UserRepository;
import me.modernpage.ui.common.NextPageHandler;
import me.modernpage.ui.common.ProcessHandler;
import me.modernpage.util.AbsentLiveData;
import me.modernpage.util.LoadState;
import me.modernpage.util.Objects;
import me.modernpage.util.Resource;

@HiltViewModel
public class GroupAddEditViewModel extends ViewModel {
    private static final String TAG = "GroupAddEditViewModel";
    private final LiveData<Resource<Profile>> user;
    private LiveData<Resource<LoadModel<Profile>>> followers;
    private final LiveData<Uri> uploadedImage;
    private final SavedStateHandle mSavedStateHandle;
    private final CreateGroupHandler mCreateGroupHandler;
    private final UserRepository mUserRepository;
    private final FollowersNextPageHandler mFollowersNextPageHandler;
    private final RefreshHandler mRefreshHandler;
    private final MutableLiveData<String> groupName;

    @Inject
    public GroupAddEditViewModel(GroupRepository groupRepository, SavedStateHandle savedStateHandle, UserRepository userRepository) {
        mSavedStateHandle = savedStateHandle;
        uploadedImage = savedStateHandle.getLiveData("uploadedImage");
        mCreateGroupHandler = new CreateGroupHandler(groupRepository);
        mUserRepository = userRepository;
        mFollowersNextPageHandler = new FollowersNextPageHandler(userRepository);
        mRefreshHandler = new RefreshHandler(userRepository);
        LiveData<Long> uid = savedStateHandle.getLiveData("uid");
        user = Transformations.switchMap(uid, input -> {
            if (input == null)
                return AbsentLiveData.create();
            return userRepository.getUserById(input);
        });

        followers = Transformations.switchMap(user, input -> {
            if (input == null || input.data == null)
                return AbsentLiveData.create();
            return mUserRepository.getUsers(input.data._followers());
        });

        groupName = savedStateHandle.getLiveData("groupName");
    }

    public MutableLiveData<String> getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        mSavedStateHandle.set("groupName", groupName);
    }

    public LiveData<Resource<LoadModel<Profile>>> getFollowers() {
        return followers;
    }

    public void setUid(long uid) {
        mSavedStateHandle.set("uid", uid);
    }

    public void setUploadedImage(Uri result) {
        mSavedStateHandle.set("uploadedImage", result);
    }

    public LiveData<Uri> getUploadedImage() {
        return uploadedImage;
    }

    public void createGroup(PrivateGroup privateGroup, File groupImage) {
        mCreateGroupHandler.createGroup(privateGroup, groupImage);
    }

    public LiveData<Resource<Profile>> getUser() {
        return user;
    }

    public LiveData<LoadState<Boolean>> getCreateGroupState() {
        return mCreateGroupHandler.getProcessState();
    }

    public void relogin() {
        mUserRepository.relogin();
    }

    public void refresh() {
        followers = Transformations.switchMap(user, input -> {
            if (input == null || input.data == null)
                return AbsentLiveData.create();
            return mUserRepository.getUsers(input.data._followers());
        });
    }

    public void pullToRefresh() {
        String value = user.getValue() == null || user.getValue().data == null
                ? null : user.getValue().data._followers();
        if (value == null || value.trim().length() == 0) {
            return;
        }
        mRefreshHandler.refreshUsers(value);
    }

    public void loadFollowersNextPage() {
        String value = user.getValue() == null || user.getValue().data == null
                ? null : user.getValue().data._followers();
        if (value == null || value.trim().length() == 0) {
            return;
        }
        mFollowersNextPageHandler.loadPage(value);
    }

    static class CreateGroupHandler extends ProcessHandler<Boolean> {
        private GroupRepository repository;

        public CreateGroupHandler(GroupRepository repository) {
            super();
            this.repository = repository;
        }

        public void createGroup(PrivateGroup privateGroup, File groupImage) {
            unregister();
            data = repository.saveGroup(privateGroup, groupImage);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }
    }

    static class FollowersNextPageHandler extends NextPageHandler {
        private final UserRepository repository;

        public FollowersNextPageHandler(UserRepository repository) {
            super();
            this.repository = repository;
        }

        void loadPage(String url) {
            if (Objects.equals(this.url, url)) {
                return;
            }
            unregister();
            data = repository.loadUsersNextPage(url);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }
    }

    static class RefreshHandler extends ProcessHandler<Boolean> {
        @Nullable
        private String url;
        private final UserRepository repository;

        RefreshHandler(UserRepository repository) {
            super();
            this.repository = repository;
        }

        void refreshUsers(String url) {
            if (Objects.equals(this.url, url))
                return;
            unregister();
            data = repository.refreshUsers(url);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }
    }

    public LiveData<LoadState<Boolean>> getLoadMoreStatus() {
        return mFollowersNextPageHandler.getProcessState();
    }

    public LiveData<LoadState<Boolean>> getRefreshState() {
        return mRefreshHandler.getProcessState();
    }
}