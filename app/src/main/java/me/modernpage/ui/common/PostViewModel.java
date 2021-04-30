package me.modernpage.ui.common;

import androidx.lifecycle.LiveData;

import javax.inject.Inject;

import me.modernpage.data.local.entity.model.Likes;
import me.modernpage.data.local.entity.model.LoadModel;
import me.modernpage.data.local.entity.relation.CommentRelation;
import me.modernpage.data.repository.PostRepository;
import me.modernpage.util.Resource;


public class PostViewModel {
    private PostRepository mPostRepository;
    private final PostLikeHandler mPostLikeHandler;
    private static final String TAG = "PostViewModel";

    @Inject
    public PostViewModel(PostRepository postRepository) {
        mPostLikeHandler = new PostLikeHandler(postRepository);
        mPostRepository = postRepository;
    }

    public LiveData<Resource<Likes>> getLikes(String url, long ownerId) {
        return mPostRepository.getPostLikes(url, ownerId);
    }

    public LiveData<Resource<LoadModel<CommentRelation>>> getComments(String url) {
        return mPostRepository.getPostComments(url);
    }


    public void likeControl(String url, long postId, long ownerId) {
        mPostLikeHandler.likeControl(url, postId, ownerId);
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
}
