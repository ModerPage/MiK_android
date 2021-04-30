package me.modernpage.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.model.LoadModel;
import me.modernpage.data.local.entity.relation.PostRelation;
import me.modernpage.data.repository.PostRepository;
import me.modernpage.data.repository.UserRepository;
import me.modernpage.ui.common.ProcessHandler;
import me.modernpage.util.AbsentLiveData;
import me.modernpage.util.LoadState;
import me.modernpage.util.Resource;

@HiltViewModel
public class MainViewModel extends ViewModel {
    private final LiveData<Resource<Profile>> mUserData;
    private final LiveData<Resource<LoadModel<PostRelation>>> posts;
    private final LiveData<Resource<LoadModel<Profile>>> followers;
    private final LiveData<Resource<LoadModel<Profile>>> following;
    private final SavedStateHandle mSavedStateHandle;
    private final LogoutHandler mLogoutHandler;
    private final UserRepository mUserRepository;
    private final PostRepository mPostRepository;

    @Inject
    public MainViewModel(UserRepository userRepository, SavedStateHandle savedStateHandle, PostRepository postRepository) {
        mSavedStateHandle = savedStateHandle;
        mLogoutHandler = new LogoutHandler(userRepository);
        mPostRepository = postRepository;
        LiveData<Long> usernameLiveData = mSavedStateHandle.getLiveData("uid");
        mUserData = Transformations.switchMap(usernameLiveData, uid -> {
            if (uid == null)
                return AbsentLiveData.create();
            else
                return userRepository.getUserById(uid);
        });

        posts = Transformations.switchMap(mUserData, input -> {
            if (input == null || input.data == null)
                return AbsentLiveData.create();
            return postRepository.getPosts(input.data._posts());
        });

        followers = Transformations.switchMap(mUserData, input -> {
            if (input == null || input.data == null)
                return AbsentLiveData.create();
            return userRepository.getUsers(input.data._followers());
        });

        following = Transformations.switchMap(mUserData, input -> {
            if (input == null || input.data == null)
                return AbsentLiveData.create();
            return userRepository.getUsers(input.data._following());
        });
        mUserRepository = userRepository;
    }

    public void setUid(long uid) {
        mSavedStateHandle.set("uid", uid);
    }

    public LiveData<Resource<Profile>> getUserData() {
        return mUserData;
    }

    public LiveData<Resource<LoadModel<PostRelation>>> getPosts() {
        return posts;
    }

    public void logout() {
        mLogoutHandler.logout();
    }

    public LiveData<LoadState<Boolean>> logoutState() {
        return mLogoutHandler.getProcessState();
    }

    public void relogin() {
        mUserRepository.relogin();
    }

    public LiveData<Resource<LoadModel<Profile>>> getFollowers() {
        return followers;
    }

    public LiveData<Resource<LoadModel<Profile>>> getFollowing() {
        return following;
    }

    static class LogoutHandler extends ProcessHandler<Boolean> {
        private final UserRepository repository;

        LogoutHandler(UserRepository repository) {
            this.repository = repository;
        }

        public void logout() {
            unregister();
            data = repository.logout();
            processState.setValue(new LoadState<Boolean>(true, true, null, null));
            data.observeForever(this);
        }
    }
}
