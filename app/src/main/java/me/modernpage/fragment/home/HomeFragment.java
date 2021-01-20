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
import androidx.recyclerview.widget.RecyclerView;

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
    private RecyclerView mRecyclerView;
    private HomePostRecViewAdapter mRecViewAdapter;
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
        mRecViewAdapter = new HomePostRecViewAdapter(getContext(), mPosts);
        mRecyclerView.setAdapter(mRecViewAdapter);
        SpaceItemDecoration mSpaceItemDecoration = new SpaceItemDecoration(8);
        mRecyclerView.addItemDecoration(mSpaceItemDecoration);
    }

    @Override
    public void onAddedNewPost(Post post) {
        mPosts.add(post);
        mRecViewAdapter.loadNewPost(mPosts);
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
        mRecViewAdapter.loadNewPost(posts);
    }
}
