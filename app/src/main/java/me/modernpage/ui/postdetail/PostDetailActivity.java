package me.modernpage.ui.postdetail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivityPostDetailBinding;
import me.modernpage.activity.databinding.LayoutPostMoreBottomSheetBinding;
import me.modernpage.data.local.entity.Comment;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.relation.PostRelation;
import me.modernpage.ui.BaseActivity;
import me.modernpage.ui.BaseDialog;
import me.modernpage.ui.common.PostRecyclerViewScrollListener;
import me.modernpage.ui.common.SpaceItemDecoration;
import me.modernpage.ui.login.LoginActivity;

@AndroidEntryPoint
public class PostDetailActivity extends BaseActivity<ActivityPostDetailBinding>
        implements PostDetailHandler, BaseDialog.DialogEvents {
    private static final String TAG = "PostDetailActivity";
    PostDetailViewModel viewModel;
    private BottomSheetDialog mBottomSheetDialog;
    private LayoutPostMoreBottomSheetBinding mMoreBottomSheetBinding;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_post_detail;
    }

    @Override
    public void onShareClicked(PostRelation post) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        String shareData = post.getPost().getText() + " by " + post.getOwner().getFullname() + ". "
                + post.getPost()._file() + " On MiK app";
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareData);
        sendIntent.setType("text/*");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);
    }

    @Override
    public void onPositiveDialogResult(int dialogId) {

    }

    @Override
    public void onNegativeDialogResult(int dialogId) {

    }

    public interface Handler {
        void onAddComment();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        final PostRelation post;
        Bundle args = getIntent().getExtras();
        final long uid;
        final String postLoadResultUrl;
        if (args != null) {
            post = (PostRelation) args.getSerializable(PostRelation.class.getSimpleName());
            uid = args.getLong(Profile.class.getSimpleName());
            postLoadResultUrl = args.getString(LoadResult.class.getSimpleName());
            if (post == null || postLoadResultUrl == null || uid == 0) {
                throw new IllegalArgumentException(
                        "Post data and/or uid and/or post load result url not present in bundle");
            }
        } else {
            throw new IllegalArgumentException(
                    "Post data and/or uid and/or post load result url must be passed in bundle");
        }

        viewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);

        FragmentManager fragmentManager = getSupportFragmentManager();
        switch (post.getFile().getType()) {
            case "application":
            case "video":
                fragmentManager.beginTransaction().replace(dataBinding.fragmentContainerView.getId(),
                        VideoPostDetailFragment.class, args).commit();
                break;
            case "image":
                fragmentManager.beginTransaction().replace(dataBinding.fragmentContainerView.getId(),
                        ImagePostDetailFragment.class, args).commit();
        }

        CommentListAdapter commentListAdapter = new CommentListAdapter(this.dataBindingComponent, comment -> {
            viewModel.deleteComment(post.getPost()._comments(), comment.getComment());
        }, uid);

        dataBinding.postDetailCommentRecview.setAdapter(commentListAdapter);
        SpaceItemDecoration decoration = new SpaceItemDecoration(12);
        dataBinding.postDetailCommentRecview.addItemDecoration(decoration);
        dataBinding.postDetailCommentRecview.setHasFixedSize(true);
        dataBinding.setCallback(() -> viewModel.refresh());
        dataBinding.postDetailSwipeRefresh.setOnRefreshListener(() -> viewModel.pullToRefresh());
        viewModel.setCommentQuery(post.getPost()._comments());
        dataBinding.setHandler(() -> {
            String commentText = dataBinding.postDetailCommentEdittext.getText().toString();
            if (commentText == null || commentText.trim().length() == 0)
                return;
            Profile user = dataBinding.getUser();
            if (user == null)
                return;

            Comment comment = new Comment();
            comment.setText(commentText);
            comment.setOwner(user);
            comment.setPost(post.getPost());
            viewModel.saveComment(post.getPost()._comments(), comment);
        });

        dataBinding.postDetailCommentRecview.addOnScrollListener(new PostRecyclerViewScrollListener() {
            @Override
            public void onItemIsFirstVisibleItem(int index) {
            }

            @Override
            public void onItemIsLastPosition(int index) {
                if (index == commentListAdapter.getItemCount() - 1) {
                    viewModel.loadCommentsNextPage();
                }
            }
        });

        viewModel.getComments().observe(this, commentsResource -> {
            dataBinding.setLoadResource(commentsResource);
            int resultCount = (commentsResource == null || commentsResource.data == null) ? 0 :
                    commentsResource.data.getContents().size();
            dataBinding.setResultCount(resultCount);
            commentListAdapter.replace(commentsResource == null || commentsResource.data == null ? null
                    : commentsResource.data.getContents());
            dataBinding.executePendingBindings();
        });

        viewModel.getLoadMoreStatus().observe(this, state -> {
            if (state == null) {
                dataBinding.setLoadingMore(false);
            } else {
                dataBinding.setLoadingMore(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (state.isVerified())
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", v -> logout()).show();
                }
            }
            dataBinding.executePendingBindings();
        });

        viewModel.getRefreshState().observe(this, state -> {
            if (state == null) {
                dataBinding.postDetailSwipeRefresh.setRefreshing(false);
            } else {
                dataBinding.postDetailSwipeRefresh.setRefreshing(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (state.isVerified())
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", v -> logout()).show();
                }
            }
            dataBinding.executePendingBindings();
        });

        viewModel.getProfile(uid).observe(this, profileResource -> {
            dataBinding.setUser(profileResource == null ? null : profileResource.data);
            dataBinding.executePendingBindings();
        });

        viewModel.commentProcessState().observe(this, state -> {
            if (state == null) {
                dataBinding.setProcessState(false);
            } else {
                dataBinding.setProcessState(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (state.isVerified()) {
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", v -> logout()).show();
                    }
                }
            }
        });

    }


    private void logout() {
        viewModel.relogin();
        Intent intent = new Intent(PostDetailActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
