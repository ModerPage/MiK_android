package me.modernpage.ui.postdetail;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import me.modernpage.data.local.entity.Comment;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.model.Likes;
import me.modernpage.data.local.entity.model.LoadModel;
import me.modernpage.data.local.entity.relation.CommentRelation;
import me.modernpage.data.repository.PostRepository;
import me.modernpage.data.repository.UserRepository;
import me.modernpage.ui.common.ProcessHandler;
import me.modernpage.util.AbsentLiveData;
import me.modernpage.util.LoadState;
import me.modernpage.util.Objects;
import me.modernpage.util.Resource;

@HiltViewModel
public class PostDetailViewModel extends ViewModel {
    private final LiveData<Resource<LoadModel<CommentRelation>>> comments;
    private final PostRepository mPostRepository;
    private final UserRepository mUserRepository;
    private final PostLikeHandler mPostLikeHandler;
    private final SavedStateHandle mSavedStateHandle;
    private final NextPageHandler mNextPageHandler;
    private final RefreshHandler mRefreshHandler;
    private final PostCommentHandler mCommentHandler;

    @Inject
    public PostDetailViewModel(PostRepository postRepository, SavedStateHandle savedStateHandle, UserRepository userRepository) {
        mPostRepository = postRepository;
        mUserRepository = userRepository;
        mPostLikeHandler = new PostLikeHandler(postRepository);
        mNextPageHandler = new NextPageHandler(postRepository);
        mRefreshHandler = new RefreshHandler(postRepository);
        mCommentHandler = new PostCommentHandler(postRepository);
        mSavedStateHandle = savedStateHandle;
        LiveData<String> commentQuery = mSavedStateHandle.getLiveData("commentQuery");
        comments = Transformations.switchMap(commentQuery, input -> {
            if (input == null || input.trim().length() == 0)
                return AbsentLiveData.create();
            return postRepository.getPostComments(input);
        });
    }

    public LiveData<Resource<Likes>> getLikes(String url, long ownerId) {
        return mPostRepository.getPostLikes(url, ownerId);
    }


    public LiveData<Resource<LoadModel<CommentRelation>>> getComments() {
        return comments;
    }

    public void likeControl(String url, long postId, long ownerId) {
        mPostLikeHandler.likeControl(url, postId, ownerId);
    }

    public LiveData<Resource<Profile>> getProfile(long uid) {
        return mUserRepository.getUserById(uid);
    }

    public void saveComment(String url, Comment comment) {
        mCommentHandler.saveComment(url, comment);
    }

    public LiveData<LoadState<Boolean>> commentProcessState() {
        return mCommentHandler.getProcessState();
    }

    public void refresh() {
        String commentQuery = mSavedStateHandle.get("commentQuery");
        if (commentQuery != null)
            mSavedStateHandle.set("commentQuery", commentQuery);
    }

    public void pullToRefresh() {
        String value = mSavedStateHandle.get("commentQuery");
        if (value == null || value.trim().length() == 0) {
            return;
        }
        mRefreshHandler.refreshPosts(value);
    }

    public void setCommentQuery(String commentQuery) {
        String curQuery = mSavedStateHandle.get("commentQuery");
        if (Objects.equals(curQuery, commentQuery)) {
            return;
        }
        mSavedStateHandle.set("commentQuery", commentQuery);
    }

    public void deleteComment(String url, Comment comment) {
        mCommentHandler.deleteComment(url, comment);
    }


    static class PostLikeHandler extends ProcessHandler<Boolean> {
        private final PostRepository repository;

        public PostLikeHandler(PostRepository repository) {
            this.repository = repository;
        }

        void likeControl(String url, long postId, long ownerId) {
            unregister();
            data = repository.likeControl(url, postId, ownerId);
            data.observeForever(this);
        }
    }

    static class PostCommentHandler extends ProcessHandler<Boolean> {
        private final PostRepository repository;

        public PostCommentHandler(PostRepository repository) {
            this.repository = repository;
        }

        public void saveComment(String url, Comment comment) {
            unregister();
            data = repository.saveComment(url, comment);
            data.observeForever(this);
        }

        public void deleteComment(String url, Comment comment) {
            unregister();
            data = repository.deleteComment(url, comment);
            data.observeForever(this);
        }
    }


    public void loadCommentsNextPage() {
        String value = mSavedStateHandle.get("commentQuery");
        if (value == null || value.trim().length() == 0) {
            return;
        }
        mNextPageHandler.loadNextPage(value);
    }

    public LiveData<LoadState<Boolean>> getLoadMoreStatus() {
        return mNextPageHandler.getProcessState();
    }

    static class NextPageHandler extends ProcessHandler<Boolean> {
        @Nullable
        private String url;
        private final PostRepository repository;
        boolean hasMore;

        public NextPageHandler(PostRepository repository) {
            super();
            this.repository = repository;
        }

        void loadNextPage(String url) {
            if (Objects.equals(this.url, url)) {
                return;
            }
            unregister();
            data = repository.loadCommentsNextPage(url);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }

        @Override
        public void onChanged(Resource<Boolean> result) {
            if (result == null) {
                reset();
            } else {
                switch (result.status) {
                    case SUCCESS:
                        hasMore = Boolean.TRUE.equals(result.data);
                        unregister();
                        processState.setValue(new LoadState<Boolean>(false, true, null, result.data));
                        break;
                    case ERROR:
                        hasMore = true;
                        unregister();
                        processState.setValue(new LoadState<Boolean>(false,
                                true, result.message, result.data));
                        break;
                    case LOGOUT:
                        hasMore = true;
                        unregister();
                        processState.setValue(new LoadState<>(false, false, result.message, null));
                        break;
                }
            }

        }

        @Override
        protected void unregister() {
            if (data != null) {
                data.removeObserver(this);
                data = null;
                if (hasMore) {
                    url = null;
                }
            }
        }

        @Override
        protected void reset() {
            unregister();
            hasMore = true;
            processState.setValue(new LoadState<Boolean>(false, true, null, null));
        }

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
            data = mPostRepository.refreshComments(url);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }
    }

    public LiveData<LoadState<Boolean>> getRefreshState() {
        return mRefreshHandler.getProcessState();
    }

    public void relogin() {
        mUserRepository.relogin();
    }
}
