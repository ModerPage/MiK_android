package me.modernpage.fragment.home;


import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import me.modernpage.activity.R;
import me.modernpage.entity.Post;
import me.modernpage.entity.UserEntity;
import me.modernpage.fragment.post.PostFragment;
import me.modernpage.task.GetPosts;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements PostFragment.OnAddedNewPost, GetPosts.OnGetPosts {
    private static final String TAG = "HomeFragment";

    private static final int PAGE_SIZE = 5;
    private static final String CURRENTPAGE_EXTRA = "currentpage";
    private static final String ISLASTPAGE_EXTRA = "islastpage";
    private boolean mIsLoading = false;
    private boolean mIsLastPage = false;
    private int mCurrentPage = 0;
    private HomeRecyclerView mRecyclerView;
    private HomeRecyclerViewAdapter mRecViewAdapter;
    private LinearLayoutManager mLayoutManager;
    private ProgressBar mProgressBar;

    private RecyclerView.OnScrollListener mRecyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = mLayoutManager.getChildCount();
            int totalCount = mLayoutManager.getItemCount();
            int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

            if (!mIsLoading && !mIsLastPage)
                if ((firstVisibleItemPosition + visibleItemCount) >= totalCount)
                    loadMorePosts();
        }
    };

    private void loadMorePosts() {
        mProgressBar.setVisibility(View.VISIBLE);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mIsLoading = true;
        GetPosts getPosts = new GetPosts(this);
        getPosts.execute(createUri(mCurrentPage * PAGE_SIZE + 1));
        mCurrentPage++;
    }

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

        mRecyclerView = view.findViewById(R.id.home_post_list);
        mProgressBar = view.findViewById(R.id.home_progressbar);
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecViewAdapter = new HomeRecyclerViewAdapter(new UserEntity(), initGlide()); // TODO add current user
        mRecyclerView.setAdapter(mRecViewAdapter);
        SpaceItemDecoration mSpaceItemDecoration = new SpaceItemDecoration(12);
        mRecyclerView.addItemDecoration(mSpaceItemDecoration);
        mRecyclerView.addOnScrollListener(mRecyclerViewOnScrollListener);

        if (savedInstanceState != null) {
            mCurrentPage = savedInstanceState.getInt(CURRENTPAGE_EXTRA);
            mIsLastPage = savedInstanceState.getBoolean(ISLASTPAGE_EXTRA);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadMorePosts();
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
        mRecViewAdapter.addPostFront(post);
        mRecyclerView.getPosts().add(0, post);
        mRecyclerView.scrollToPosition(0);
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
        if (posts.size() < PAGE_SIZE)
            mIsLastPage = true;
        mRecViewAdapter.addPosts(posts);
        mRecyclerView.getPosts().addAll(posts);
        mIsLoading = false;

        mProgressBar.setVisibility(View.GONE);
        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    public void onDestroy() {
        if (mRecyclerView != null) {
            mRecyclerView.releasePlayer();
        }
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(CURRENTPAGE_EXTRA, mCurrentPage);
        outState.putBoolean(ISLASTPAGE_EXTRA, mIsLastPage);
        super.onSaveInstanceState(outState);
    }

    public void stopPlay() {
        if (mRecyclerView != null)
            mRecyclerView.stopPlaying();
    }
}
