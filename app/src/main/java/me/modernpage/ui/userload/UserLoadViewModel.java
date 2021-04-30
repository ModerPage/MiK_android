package me.modernpage.ui.userload;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.model.LoadModel;
import me.modernpage.data.repository.UserRepository;
import me.modernpage.ui.common.NextPageHandler;
import me.modernpage.ui.common.ProcessHandler;
import me.modernpage.util.AbsentLiveData;
import me.modernpage.util.LoadState;
import me.modernpage.util.Objects;
import me.modernpage.util.Resource;

@HiltViewModel
public class UserLoadViewModel extends ViewModel {
    private final LiveData<Resource<LoadModel<Profile>>> data;
    private final SavedStateHandle savedStateHandle;
    private final UserRepository mUserRepository;
    private final DataNextPageHandler mDataNextPageHandler;
    private final RefreshHandler mRefreshHandler;

    @Inject
    public UserLoadViewModel(UserRepository userRepository, SavedStateHandle savedStateHandle) {
        this.savedStateHandle = savedStateHandle;
        mUserRepository = userRepository;
        mDataNextPageHandler = new DataNextPageHandler(userRepository);
        mRefreshHandler = new RefreshHandler(userRepository);
        LiveData<String> loadQuery = savedStateHandle.getLiveData("loadQuery");
        data = Transformations.switchMap(loadQuery, input -> {
            if (input == null)
                return AbsentLiveData.create();
            return userRepository.getUsers(input);
        });
    }

    public void setLoadQuery(String loadQuery) {
        String curQuery = savedStateHandle.get("loadQuery");
        if (Objects.equals(curQuery, loadQuery))
            return;
        savedStateHandle.set("loadQuery", loadQuery);
    }

    public LiveData<Resource<LoadModel<Profile>>> getData() {
        return data;
    }

    public void relogin() {
        mUserRepository.relogin();
    }

    public void loadFollowersNextPage() {
        String value = savedStateHandle.get("loadQuery");
        if (value == null || value.trim().length() == 0) {
            return;
        }
        mDataNextPageHandler.loadPage(value);
    }

    public void refresh() {
        String value = savedStateHandle.get("loadQuery");
        if (value != null)
            savedStateHandle.set("loadQuery", value);
    }

    public void pullToRefresh() {
        String value = savedStateHandle.get("loadQuery");
        if (value == null || value.trim().length() == 0) {
            return;
        }
        mRefreshHandler.refreshUsers(value);
    }

    static class DataNextPageHandler extends NextPageHandler {
        private final UserRepository repository;

        public DataNextPageHandler(UserRepository repository) {
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
        return mDataNextPageHandler.getProcessState();
    }

    public LiveData<LoadState<Boolean>> getRefreshState() {
        return mRefreshHandler.getProcessState();
    }
}
