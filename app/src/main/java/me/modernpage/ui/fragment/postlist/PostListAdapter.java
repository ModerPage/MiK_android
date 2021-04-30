package me.modernpage.ui.fragment.postlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.DataBindingComponent;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import java.util.Map;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.LayoutPostImageViewBinding;
import me.modernpage.activity.databinding.LayoutPostVideoViewBinding;
import me.modernpage.data.local.entity.relation.PostRelation;
import me.modernpage.ui.common.DataBoundListAdapter;
import me.modernpage.ui.common.PostViewModel;
import me.modernpage.util.Objects;

public class PostListAdapter extends DataBoundListAdapter<PostRelation, ViewDataBinding> {
    private static final String TAG = "PostListAdapter";
    private final PostClickCallback mCallback;
    private final DataBindingComponent dataBindingComponent;
    private Map<Long, PostViewModel> viewModelsMap;
    private final long uid;
    private static final int TYPE_IMAGE = 100;
    private static final int TYPE_VIDEO = 101;

    public PostListAdapter(PostClickCallback callback, DataBindingComponent dataBindingComponent, long uid) {
        mCallback = callback;
        this.dataBindingComponent = dataBindingComponent;
        this.uid = uid;
    }

    @Override
    protected ViewDataBinding createBinding(ViewGroup parent, int viewType) {
        ViewDataBinding binding;
        switch (viewType) {
            case TYPE_VIDEO:
                binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.layout_post_video_view, parent, false, dataBindingComponent);
                LayoutPostVideoViewBinding videoBinding = (LayoutPostVideoViewBinding) binding;

                videoBinding.postViewLike.setOnClickListener(view -> {
                    PostRelation post = videoBinding.getPost();

                    if (post != null && mCallback != null) {
                        PostViewModel viewModel = viewModelsMap.get(post.getPost().getId());
                        viewModel.likeControl(post.getPost()._likes(), post.getPost().getId(), uid);
                    }
                });

                videoBinding.postViewMore.setOnClickListener(view -> {
                    PostRelation post = videoBinding.getPost();
                    if (post != null && mCallback != null) {
                        mCallback.onPostMoreClick(post);
                    }
                });

                videoBinding.postViewShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PostRelation post = videoBinding.getPost();
                        if (post != null && mCallback != null) {
                            mCallback.onPostShareClick(post);
                        }
                    }
                });

                videoBinding.postViewAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PostRelation post = videoBinding.getPost();
                        if (post != null && mCallback != null) {
                            mCallback.onPostAvatarClick(post);
                        }
                    }
                });

                break;
            case TYPE_IMAGE:
                binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        R.layout.layout_post_image_view, parent, false, dataBindingComponent);
                LayoutPostImageViewBinding imageBinding = (LayoutPostImageViewBinding) binding;

                imageBinding.postViewLike.setOnClickListener(view -> {
                    PostRelation post = imageBinding.getPost();

                    if (post != null && mCallback != null) {
                        PostViewModel viewModel = viewModelsMap.get(post.getPost().getId());
                        viewModel.likeControl(post.getPost()._likes(), post.getPost().getId(), uid);
                    }
                });

                imageBinding.postViewMore.setOnClickListener(view -> {
                    PostRelation post = imageBinding.getPost();
                    if (post != null && mCallback != null) {
                        mCallback.onPostMoreClick(post);
                    }
                });

                imageBinding.postViewShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PostRelation post = imageBinding.getPost();
                        if (post != null && mCallback != null) {
                            mCallback.onPostShareClick(post);
                        }
                    }
                });

                imageBinding.postViewAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        PostRelation post = imageBinding.getPost();
                        if (post != null && mCallback != null) {
                            mCallback.onPostAvatarClick(post);
                        }
                    }
                });
                break;
            default:
                throw new IllegalArgumentException("No available type: " + viewType);
        }
        return binding;
    }

    @Override
    public void onClickView(ViewDataBinding binding, int position) {
        PostRelation post = null;
        if (binding instanceof LayoutPostVideoViewBinding) {
            post = ((LayoutPostVideoViewBinding) binding).getPost();
        } else {
            post = ((LayoutPostImageViewBinding) binding).getPost();
        }

        if (post != null && mCallback != null)
            mCallback.onPostClick(post);
    }

    @Override
    protected void bind(ViewDataBinding binding, PostRelation item, int position) {
        if (binding instanceof LayoutPostImageViewBinding) {
            ((LayoutPostImageViewBinding) binding).setPost(item);
            viewModelsMap.get(item.getPost().getId()).getLikes(item.getPost()._likes(), uid)
                    .observe(binding.getLifecycleOwner(), result -> {
                        ((LayoutPostImageViewBinding) binding).setLikes(result == null ? null : result.data);
                        binding.executePendingBindings();
                    });

            viewModelsMap.get(item.getPost().getId()).getComments(item.getPost()._comments())
                    .observe(binding.getLifecycleOwner(), result -> {
                        ((LayoutPostImageViewBinding) binding).setComments(result == null ? null : result.data);
                        binding.executePendingBindings();
                    });


        } else if (binding instanceof LayoutPostVideoViewBinding) {
            ((LayoutPostVideoViewBinding) binding).setPost(item);
            ((LayoutPostVideoViewBinding) binding).setIndex(position);
            viewModelsMap.get(item.getPost().getId()).getLikes(item.getPost()._likes(), uid)
                    .observe(binding.getLifecycleOwner(), result -> {
                        ((LayoutPostVideoViewBinding) binding).setLikes(result == null ? null : result.data);
                        binding.executePendingBindings();
                    });

            viewModelsMap.get(item.getPost().getId()).getComments(item.getPost()._comments())
                    .observe(binding.getLifecycleOwner(), result -> {
                        ((LayoutPostVideoViewBinding) binding).setComments(result == null ? null : result.data);
                        binding.executePendingBindings();
                    });
        }

    }

    @Override
    protected int getItemViewType(PostRelation item) {
        switch (item.getFile().getType()) {
            case "video":
            case "application":
                return TYPE_VIDEO;
            case "image":
                return TYPE_IMAGE;
        }
        return 0;
    }

    @Override
    protected boolean areItemsTheSame(PostRelation oldItem, int oldItemPosition, PostRelation newItem, int newItemPosition) {
        return Objects.equals(oldItem.getPost().getId(), newItem.getPost().getId());
    }

    @Override
    protected boolean areContentsTheSame(PostRelation oldItem, int oldItemPosition, PostRelation newItem, int newItemPosition) {
        return Objects.equals(oldItem.getPost().getText(), newItem.getPost().getText()) &&
                Objects.equals(oldItem.getFile().getName(), newItem.getFile().getName()) &&
                Objects.equals(oldItem.getLocation(), newItem.getLocation());
    }

    public interface PostClickCallback {
        void onPostClick(PostRelation post);

        void onPostMoreClick(PostRelation post);

        void onPostShareClick(PostRelation post);

        void onPostAvatarClick(PostRelation post);
    }

    public void setViewModelsMap(Map<Long, PostViewModel> viewModelsMap) {
        this.viewModelsMap = viewModelsMap;
    }

}
