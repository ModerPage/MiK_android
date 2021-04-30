package me.modernpage.ui.fragment.postlist;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import me.modernpage.data.local.entity.Post;
import me.modernpage.data.local.entity.model.LoadModel;
import me.modernpage.data.local.entity.relation.PostRelation;
import me.modernpage.data.repository.PostRepository;
import me.modernpage.data.repository.UserRepository;
import me.modernpage.ui.common.NextPageHandler;
import me.modernpage.ui.common.PostViewModel;
import me.modernpage.ui.common.ProcessHandler;
import me.modernpage.util.AbsentLiveData;
import me.modernpage.util.LoadState;
import me.modernpage.util.Objects;
import me.modernpage.util.Resource;

@HiltViewModel
public class PostListViewModel extends ViewModel {
    private static final String TAG = "PostListViewModel";
    private final LiveData<Resource<LoadModel<PostRelation>>> posts;
    private final MutableLiveData<Map<Long, PostViewModel>> postViewModelsMap =
            new MutableLiveData<>(new HashMap<>());
    private final SavedStateHandle mSavedStateHandle;
    private final PostRepository mPostRepository;
    private final UserRepository mUserRepository;
    private final PostNextPageHandler mPostNextPageHandler;
    private final ProcessPostHandler mProcessPostHandler;
    private final RefreshHandler mRefreshHandler;

    @Inject
    public PostListViewModel(PostRepository postRepository, UserRepository userRepository, SavedStateHandle savedStateHandle) {
        mPostNextPageHandler = new PostNextPageHandler(postRepository);
        mProcessPostHandler = new ProcessPostHandler(postRepository);
        mSavedStateHandle = savedStateHandle;
        mPostRepository = postRepository;
        mUserRepository = userRepository;
        mRefreshHandler = new RefreshHandler(postRepository);

        LiveData<String> postQuery = mSavedStateHandle.getLiveData("postQuery");
        posts = Transformations.switchMap(postQuery, input -> {
            if (input == null || input.trim().length() == 0)
                return AbsentLiveData.create();
            return postRepository.getPosts(input);
        });
    }

    public void setPosts(List<PostRelation> posts) {
        if (posts == null)
            return;

        Map<Long, PostViewModel> temp = postViewModelsMap.getValue();
        for (PostRelation post : posts) {
            if (!temp.containsKey(post.getPost().getId())) {
                temp.put(post.getPost().getId(), new PostViewModel(mPostRepository));
            }
        }
        postViewModelsMap.setValue(temp);
    }

    public MutableLiveData<Map<Long, PostViewModel>> getPostViewModelsMap() {
        return postViewModelsMap;
    }


    public void setPostQuery(String postQuery) {
        String curQuery = mSavedStateHandle.get("postQuery");
        if (Objects.equals(curQuery, postQuery))
            return;
        mSavedStateHandle.set("postQuery", postQuery);
    }

    public LiveData<Resource<LoadModel<PostRelation>>> getPosts() {
        return posts;
    }


    public void loadPostsNextPage() {
        String value = mSavedStateHandle.get("postQuery");
        if (value == null || value.trim().length() == 0) {
            return;
        }
        mPostNextPageHandler.loadNextPage(value);
    }

    public LiveData<LoadState<Boolean>> getLoadMoreStatus() {
        return mPostNextPageHandler.getProcessState();
    }

    public void refresh() {
        String postQuery = mSavedStateHandle.get("postQuery");
        if (postQuery != null)
            mSavedStateHandle.set("postQuery", postQuery);
    }

    public void pullToRefresh() {
        String value = mSavedStateHandle.get("postQuery");
        if (value == null || value.trim().length() == 0) {
            return;
        }
        mRefreshHandler.refreshPosts(value);
    }

    public LiveData<LoadState<Boolean>> getRefreshState() {
        return mRefreshHandler.getProcessState();
    }

    public void hidePost(String url, long uid, Post post) {
        mProcessPostHandler.hidePost(url, uid, post);
    }

    static class RefreshHandler extends ProcessHandler<Boolean> {
        @Nullable
        private String url;
        private final PostRepository mPostRepository;

        RefreshHandler(PostRepository postRepository) {
            super();
            mPostRepository = postRepository;
        }

        void refreshPosts(String url) {
            if (Objects.equals(this.url, url))
                return;
            unregister();
            data = mPostRepository.refreshPosts(url);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }
    }

    static class PostNextPageHandler extends NextPageHandler {

        private final PostRepository repository;

        public PostNextPageHandler(PostRepository repository) {
            super();
            this.repository = repository;
        }

        void loadNextPage(String url) {
            if (Objects.equals(this.url, url)) {
                return;
            }
            unregister();
            data = repository.loadPostsNextPage(url);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }
    }

    /**
     * @param url  post load result primary key
     * @param post deleting post data
     */
    public void deletePost(String url, Post post) {
        mProcessPostHandler.deletePost(url, post);
    }


    public LiveData<LoadState<Boolean>> getProcessState() {
        return mProcessPostHandler.getProcessState();
    }

    static class ProcessPostHandler extends ProcessHandler<Boolean> {
        private final PostRepository repository;

        public ProcessPostHandler(PostRepository repository) {
            super();
            this.repository = repository;
        }

        public void deletePost(String url, Post post) {
            unregister();
            data = repository.deletePost(url, post);
            processState.setValue(new LoadState<Boolean>(true, true, null, null));
            data.observeForever(this);
        }

        public void hidePost(String url, long uid, Post post) {
            unregister();
            data = repository.hidePost(url, uid, post);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }
    }

    public void relogin() {
        mUserRepository.relogin();
    }
}
