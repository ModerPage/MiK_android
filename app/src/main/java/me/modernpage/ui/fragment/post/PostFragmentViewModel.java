package me.modernpage.ui.fragment.post;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.io.File;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.Location;
import me.modernpage.data.local.entity.Post;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.model.LoadModel;
import me.modernpage.data.local.entity.relation.PostRelation;
import me.modernpage.data.repository.GroupRepository;
import me.modernpage.data.repository.PostRepository;
import me.modernpage.data.repository.UserRepository;
import me.modernpage.ui.common.ProcessHandler;
import me.modernpage.util.AbsentLiveData;
import me.modernpage.util.LoadState;
import me.modernpage.util.Objects;
import me.modernpage.util.Resource;

@HiltViewModel
public class PostFragmentViewModel extends ViewModel {
    private final LiveData<Resource<Profile>> user;
    private final MutableLiveData<Integer> groupIndex;
    private final MutableLiveData<String> text;
    private final MutableLiveData<File> file;
    private final MutableLiveData<Location> location;
    private final SavedStateHandle mSavedStateHandle;
    private final GroupRepository mGroupRepository;
    private final UserRepository mUserRepository;
    private final ProcessPostHandler mProcessPostHandler;
    private PostRelation mPost;

    @Inject
    public PostFragmentViewModel(UserRepository userRepository, GroupRepository groupRepository, PostRepository postRepository, SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mGroupRepository = groupRepository;
        mUserRepository = userRepository;
        mProcessPostHandler = new ProcessPostHandler(postRepository);
        LiveData<Long> uid = mSavedStateHandle.getLiveData("uid");
        user = Transformations.switchMap(uid, id -> {
            if (id != null) {
                return userRepository.getUserById(id);
            }
            return AbsentLiveData.create();
        });

        groupIndex = mSavedStateHandle.getLiveData("groupIndex");
        text = mSavedStateHandle.getLiveData("text");
        file = mSavedStateHandle.getLiveData("file");
        location = mSavedStateHandle.getLiveData("location");
    }

    public LiveData<Resource<Profile>> getUser() {
        return user;
    }

    public void setUid(Long uid) {
        Long cur = mSavedStateHandle.get("uid");
        if (Objects.equals(cur, uid))
            return;
        mSavedStateHandle.set("uid", uid);
    }

    public MutableLiveData<Integer> getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(int index) {
        mSavedStateHandle.set("groupIndex", index);
    }

    public MutableLiveData<String> getText() {
        return text;
    }

    public void setText(String text) {
        mSavedStateHandle.set("text", text);
    }

    public LiveData<File> getFile() {
        return file;
    }

    public void setFile(File file) {
        mSavedStateHandle.set("file", file);
    }

    public LiveData<Location> getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        mSavedStateHandle.set("location", location);
    }

    public PostRelation getPost() {
        return mPost;
    }

    public void setPost(PostRelation post) {
        mPost = post;
    }

    public void createPost(String postLoadResultId, String postUrl, Post post, File file) {
        mProcessPostHandler.createPost(postLoadResultId, postUrl, post, file);
    }

    public void updatePost(String postUrl, Post post, File file) {
        mProcessPostHandler.updatePost(postUrl, post, file);
    }

    public LiveData<LoadState<Boolean>> getProcessState() {
        return mProcessPostHandler.getProcessState();
    }


    public LiveData<Resource<LoadModel<Group>>> getGroups() {
        return Transformations.switchMap(user, input -> {
            if (input == null || input.data == null)
                return AbsentLiveData.create();
            return mGroupRepository.getUserGroups(input.data._groups());
        });
    }

    public void relogin() {
        mUserRepository.relogin();
    }

    public void reset() {
        mProcessPostHandler.reset();
        text.setValue(null);
        groupIndex.setValue(null);
        file.setValue(null);
        location.setValue(null);
    }

    static class ProcessPostHandler extends ProcessHandler<Boolean> {
        private static final String TAG = "ProcessPostHandler";
        private final PostRepository repository;

        public ProcessPostHandler(PostRepository repository) {
            super();
            this.repository = repository;
        }

        public void createPost(String postLoadResultId, String postsUrl, Post post, File file) {
            Log.d(TAG, "createPost: " + postsUrl);
            unregister();
            data = repository.createPost(postLoadResultId, postsUrl, post, file);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }

        public void updatePost(String postUrl, Post post, File file) {
            unregister();
            data = repository.updatePost(postUrl, post, file);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }

        @Override
        protected void reset() {
            super.reset();
        }
    }
}
