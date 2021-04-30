package me.modernpage.ui.fragment.group;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.data.local.entity.PublicGroup;
import me.modernpage.data.local.entity.model.LoadModel;
import me.modernpage.data.repository.GroupRepository;
import me.modernpage.data.repository.UserRepository;
import me.modernpage.ui.common.NextPageHandler;
import me.modernpage.ui.common.ProcessHandler;
import me.modernpage.util.Constants;
import me.modernpage.util.LoadState;
import me.modernpage.util.Objects;
import me.modernpage.util.Resource;

@HiltViewModel
public class GroupViewModel extends ViewModel {
    private final UserRepository mUserRepository;
    private final GroupNextPageHandler mGroupNextPageHandler;
    private final RefreshHandler mRefreshHandler;
    private final GroupRepository mGroupRepository;
    private LiveData<Resource<List<PublicGroup>>> publicGroups;
    private LiveData<Resource<LoadModel<PrivateGroup>>> privateGroups;

    @Inject
    public GroupViewModel(UserRepository userRepository, GroupRepository groupRepository) {
        mUserRepository = userRepository;
        mGroupRepository = groupRepository;
        mGroupNextPageHandler = new GroupNextPageHandler(groupRepository);
        mRefreshHandler = new RefreshHandler(groupRepository);
        publicGroups = groupRepository.getPublicGroups();
        privateGroups = groupRepository.getPrivateGroups();
    }

    public LiveData<Resource<LoadModel<PrivateGroup>>> getPrivateGroups() {
        return privateGroups;
    }

    public LiveData<Resource<List<PublicGroup>>> getPublicGroups() {
        return publicGroups;
    }

    public void relogin() {
        mUserRepository.relogin();
    }

    public LiveData<LoadState<Boolean>> getLoadMoreStatus() {
        return mGroupNextPageHandler.getProcessState();
    }

    public LiveData<LoadState<Boolean>> getRefreshState() {
        return mRefreshHandler.getProcessState();
    }

    public void loadPostsNextPage() {
        mGroupNextPageHandler.loadNextPage(Constants.Network.ENDPOINT_PRIVATE_GROUPS);
    }


    public void refresh() {
        mRefreshHandler.refreshGroups(Constants.Network.ENDPOINT_PRIVATE_GROUPS);
    }

    static class GroupNextPageHandler extends NextPageHandler {
        private final GroupRepository repository;

        GroupNextPageHandler(GroupRepository repository) {
            this.repository = repository;
        }

        public void loadNextPage(String url) {
            if (Objects.equals(this.url, url)) {
                return;
            }
            unregister();
            data = repository.loadGroupsNextPage(url);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }
    }

    static class RefreshHandler extends ProcessHandler<Boolean> {
        @Nullable
        private String url;
        private final GroupRepository repository;

        RefreshHandler(GroupRepository repository) {
            super();
            this.repository = repository;
        }

        void refreshGroups(String url) {
            if (Objects.equals(this.url, url))
                return;
            unregister();
            data = repository.refreshGroups(url);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }
    }
}
