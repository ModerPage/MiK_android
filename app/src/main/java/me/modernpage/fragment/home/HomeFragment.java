package me.modernpage.fragment.home;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.activity.R;
import me.modernpage.entity.Post;
import me.modernpage.fragment.post.PostFragment;
import me.modernpage.task.GetPosts;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements PostFragment.OnAddedNewPost, GetPosts.OnGetPosts {
    private static final String TAG = "HomeFragment";
    private static final int PAGE_SIZE = 5;
    private HomeRecyclerView mRecyclerView;
    private HomeRecyclerViewAdapter mRecViewAdapter;
    private List<Post> mPosts;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GetPosts getPosts = new GetPosts(this);
        getPosts.execute(createUri(1));
        mPosts = new ArrayList<>();
        mRecyclerView = view.findViewById(R.id.home_post_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecViewAdapter = new HomeRecyclerViewAdapter(mPosts, initGlide());
        mRecyclerView.setAdapter(mRecViewAdapter);
        SpaceItemDecoration mSpaceItemDecoration = new SpaceItemDecoration(12);
        mRecyclerView.addItemDecoration(mSpaceItemDecoration);
    }

    private RequestManager initGlide() {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.white_background)
                .error(R.drawable.white_background);

        return Glide.with(this)
                .setDefaultRequestOptions(options);
    }

    @Override
    public void onAddedNewPost(Post post) {
        mPosts.add(post);
        mRecViewAdapter.loadNewPosts(mPosts);
    }

    private String createUri(int start) {
        Log.d(TAG, "createUri: called");

        return Uri.parse("/post/getPosts").buildUpon()
                .appendQueryParameter("start", String.valueOf(start))
                .appendQueryParameter("size", String.valueOf(PAGE_SIZE))
                .build().toString();
    }

    @Override
    public void onGetPostsComplete(List<Post> posts) {
        mRecViewAdapter.loadNewPosts(posts);
        mRecyclerView.setPosts(posts);
    }

    @Override
    public void onDestroy() {
        if (mRecyclerView != null) {
            mRecyclerView.releasePlayer();
        }
        super.onDestroy();
    }
}
