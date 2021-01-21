package me.modernpage.fragment.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;

import java.util.List;

import me.modernpage.activity.R;
import me.modernpage.entity.Post;

public class HomeRecyclerViewAdapter extends RecyclerView.Adapter<HomeRecyclerViewHolder> {
    private List<Post> mPosts;
    private RequestManager mRequestManager;

    public HomeRecyclerViewAdapter(List<Post> posts, RequestManager requestManager) {
        mPosts = posts;
        mRequestManager = requestManager;
    }

    void loadNewPosts(List<Post> posts) {
        mPosts = posts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HomeRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HomeRecyclerViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HomeRecyclerViewHolder holder, int position) {
        holder.onBind(mPosts.get(position), mRequestManager);
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }
}
