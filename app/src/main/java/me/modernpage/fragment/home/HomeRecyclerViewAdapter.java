package me.modernpage.fragment.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.activity.R;
import me.modernpage.entity.Post;
import me.modernpage.entity.UserEntity;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewHolder> {

    private UserEntity mCurrentUser;
    private List<Post> mPosts;
    private RequestManager mRequestManager;
    private static final int TYPE_IMAGE = 1;
    private static final int TYPE_VIDEO = 2;

    public HomeRecyclerViewAdapter(UserEntity currentUser, RequestManager requestManager) {
        mPosts = new ArrayList<>();
        mCurrentUser = currentUser;
        mRequestManager = requestManager;
    }

    void addPostFront(Post post) {
        mPosts.add(0, post);
        notifyItemInserted(0);
    }

    void addPosts(List<Post> posts) {
        mPosts.addAll(posts);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Post post = mPosts.get(position);
        String extension = post.getFileURL().substring(post.getFileURL().lastIndexOf(".") + 1);

        if (extension.equalsIgnoreCase("jpg") ||
                extension.equalsIgnoreCase("jpeg") ||
                extension.equalsIgnoreCase("png")) {
            return TYPE_IMAGE;
        } else if (extension.equalsIgnoreCase("mp4") ||
                extension.equalsIgnoreCase("mov") ||
                extension.equalsIgnoreCase("AVI") ||
                extension.equalsIgnoreCase("wmv") ||
                extension.equalsIgnoreCase("flv") ||
                extension.equalsIgnoreCase("webm")) {
            return TYPE_VIDEO;
        }

        return 0;
    }

    @NonNull
    @Override
    public HomeRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_IMAGE:
                return new HomeRecyclerViewHolderImage(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post_list_image, parent, false));
            case TYPE_VIDEO:
                return new HomeRecyclerViewHolderVideo(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post_list_video, parent, false));
            default:
                throw new IllegalStateException("This recycler view can't handle this view type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull HomeRecyclerViewHolder holder, int position) {
        holder.onBind(mCurrentUser, mPosts.get(position), mRequestManager);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }
}
