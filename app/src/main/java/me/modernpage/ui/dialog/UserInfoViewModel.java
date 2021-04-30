package me.modernpage.ui.dialog;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.model.Followers;
import me.modernpage.data.local.entity.model.LoadModel;
import me.modernpage.data.local.entity.relation.PostRelation;
import me.modernpage.data.repository.PostRepository;
import me.modernpage.data.repository.UserRepository;
import me.modernpage.ui.common.ProcessHandler;
import me.modernpage.util.LoadState;
import me.modernpage.util.Resource;

@HiltViewModel
public class UserInfoViewModel extends ViewModel {
    private final PostRepository mPostRepository;
    private final UserRepository mUserRepository;
    private final FollowControlHandler mFollowControlHandler;

    @Inject
    public UserInfoViewModel(PostRepository postRepository, UserRepository userRepository) {
        mPostRepository = postRepository;
        mUserRepository = userRepository;
        mFollowControlHandler = new FollowControlHandler(userRepository);
    }

    public LiveData<Resource<LoadModel<PostRelation>>> getPosts(String url) {
        return mPostRepository.getPosts(url);
    }

    public LiveData<Resource<LoadModel<Profile>>> getFollowing(String url) {
        return mUserRepository.getUsers(url);
    }

    public LiveData<Resource<Followers>> getFollowers(String url, long uid) {
        return mUserRepository.getFollowers(url, uid);
    }

    public void followControl(String url, long profileId, long uid) {
        mFollowControlHandler.followControl(url, profileId, uid);
    }

    public LiveData<LoadState<Boolean>> followControlState() {
        return mFollowControlHandler.getProcessState();
    }

    static class FollowControlHandler extends ProcessHandler<Boolean> {
        private UserRepository repository;

        public FollowControlHandler(UserRepository repository) {
            super();
            this.repository = repository;
        }

        public void followControl(String url, long profileId, long uid) {
            unregister();
            data = repository.followControl(url, profileId, uid);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }
    }
}
