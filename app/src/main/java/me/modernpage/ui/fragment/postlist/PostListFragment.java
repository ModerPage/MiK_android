package me.modernpage.ui.fragment.postlist;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.FragmentPostListBinding;
import me.modernpage.activity.databinding.LayoutPostMoreBottomSheetBinding;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.Post;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.relation.PostRelation;
import me.modernpage.ui.BaseFragment;
import me.modernpage.ui.addedit.PostAddEditActivity;
import me.modernpage.ui.common.BottomSheetHandler;
import me.modernpage.ui.common.PostRecyclerViewScrollListener;
import me.modernpage.ui.common.SpaceItemDecoration;
import me.modernpage.ui.dialog.AppDialog;
import me.modernpage.ui.dialog.PostReportDialog;
import me.modernpage.ui.dialog.UserInfoDialog;
import me.modernpage.ui.login.LoginActivity;
import me.modernpage.ui.postdetail.PostDetailActivity;
import me.modernpage.util.AutoClearedValue;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PostListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
public class PostListFragment extends BaseFragment<PostListViewModel, FragmentPostListBinding>
        implements AppDialog.DialogEvents {
    private static final String TAG = "PostListFragment";
    private static final int DIALOG_ID_DELETE = 101;
    private AutoClearedValue<PostListAdapter> adapter;
    private BottomSheetDialog bottomSheetDialog;

//    private PostListCallback mCallback;


    public PostListFragment() {
    }

    private final ActivityResultLauncher<PostRelation> addEditForResult = registerForActivityResult(
            new AddEditContract(), result -> {
                if (result != null && result) {
//                    if(mCallback != null)
//                        mCallback.onPostListReload();
                }
            }
    );

    @Override
    public void onPositiveDialogResult(int dialogId, Bundle args) {
        if (dialogId == DIALOG_ID_DELETE) {
            String postLoadUrl = requireArguments().getString(LoadResult.class.getSimpleName());
            Post post = (Post) args.getSerializable(Post.class.getSimpleName());
            if (post == null || postLoadUrl == null)
                throw new AssertionError("Post data and/or load url not present");
            viewModel.deletePost(postLoadUrl, post);
        }
    }

    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {

    }

    @Override
    public void onDialogCancelled(int dialogId) {

    }

    private static class AddEditContract extends ActivityResultContract<PostRelation, Boolean> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, PostRelation input) {
            Intent intent = new Intent(context, PostAddEditActivity.class);
            intent.putExtra(PostRelation.class.getSimpleName(), input);
            return intent;
        }

        @Override
        public Boolean parseResult(int resultCode, @Nullable Intent intent) {
            return intent == null ? null : intent.getBooleanExtra("result", false);
        }
    }


    @Override
    public int getLayoutRes() {
        return R.layout.fragment_post_list;
    }

    @Override
    public Class<PostListViewModel> getViewModel() {
        return PostListViewModel.class;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        Activity activity = (Activity) context;
//        if(!(activity instanceof PostListCallback)) {
//            throw new ClassCastException(activity.getClass().getSimpleName() +
//                    "must implement PostListFragment.PostListCallback interface");
//        }
//        mCallback = (PostListCallback) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mCallback = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        long uid;
        String postLoadResult;
        if (args != null) {
            uid = args.getLong(Profile.class.getSimpleName());
            postLoadResult = args.getString(LoadResult.class.getSimpleName());
            if (uid == 0 || postLoadResult == null)
                throw new IllegalArgumentException("Uid and/or Load posts url not present in bundle.");

        } else {
            throw new IllegalArgumentException("Uid and Load posts url must be passed in bundle.");
        }
        bottomSheetDialog = new BottomSheetDialog(getContext());
        LayoutPostMoreBottomSheetBinding moreBottomSheetBinding =
                DataBindingUtil.inflate(getLayoutInflater(), R.layout.layout_post_more_bottom_sheet, null, false);
        bottomSheetDialog.setContentView(moreBottomSheetBinding.getRoot());
        bottomSheetDialog.setCanceledOnTouchOutside(true);
        moreBottomSheetBinding.setHandler(new BottomSheetHandler() {
            @Override
            public void deletePost(PostRelation post) {
                if (bottomSheetDialog != null && bottomSheetDialog.isShowing())
                    bottomSheetDialog.dismiss();
                Fragment prev = getChildFragmentManager().findFragmentByTag("deleteDialog");
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                AppDialog dialog = new AppDialog();
                Bundle args = new Bundle();
                args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_DELETE);
                args.putString(AppDialog.DIALOG_MESSAGE,
                        getString(R.string.deldaig_message, "this post"));
                args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.deldiag_positive_caption);
                post.getPost().setOwner(post.getOwner());
                post.getPost().setGroup(post.getGroup());
                args.putSerializable(Post.class.getSimpleName(), post.getPost());
                dialog.setArguments(args);
                dialog.show(ft, "deleteDialog");
            }

            @Override
            public void editPost(PostRelation post) {
                // open new activity to edit this post
                addEditForResult.launch(post);
                if (bottomSheetDialog != null && bottomSheetDialog.isShowing())
                    bottomSheetDialog.dismiss();
            }

            @Override
            public void reportPost(PostRelation post) {
                PostReportDialog postReportDialog = PostReportDialog.newInstance(post.getPost().getId(), uid);
                postReportDialog.show(getChildFragmentManager(), null);
                if (bottomSheetDialog != null && bottomSheetDialog.isShowing())
                    bottomSheetDialog.dismiss();
            }

            @Override
            public void hidePost(PostRelation post) {
                post.getPost().setOwner(post.getOwner());
                post.getPost().setGroup(post.getGroup());
                viewModel.hidePost(postLoadResult, uid, post.getPost());
                if (bottomSheetDialog != null && bottomSheetDialog.isShowing())
                    bottomSheetDialog.dismiss();
            }
        });
        init();
        PostListAdapter postListAdapter = new PostListAdapter(new PostListAdapter.PostClickCallback() {
            @Override
            public void onPostClick(PostRelation post) {
                Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                intent.putExtra(PostRelation.class.getSimpleName(), post);
                intent.putExtra(Profile.class.getSimpleName(), uid);
                intent.putExtra(LoadResult.class.getSimpleName(), postLoadResult);
                startActivity(intent);
            }

            @Override
            public void onPostMoreClick(PostRelation post) {
                moreBottomSheetBinding.setUid(uid);
                moreBottomSheetBinding.setPostOwner(post.getOwner());
                moreBottomSheetBinding.setPost(post);
                moreBottomSheetBinding.executePendingBindings();
                bottomSheetDialog.show();
            }

            @Override
            public void onPostShareClick(PostRelation post) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                String shareData = post.getPost().getText() + " by " + post.getOwner().getFullname() + ". "
                        + post.getPost()._file() + " On MiK app";
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareData);
                sendIntent.setType("text/*");

                Intent shareIntent = Intent.createChooser(sendIntent, null);
                view.getContext().startActivity(shareIntent);
            }

            @Override
            public void onPostAvatarClick(PostRelation post) {
                if (post.getOwner().getId() == uid)
                    return;
                FragmentManager fragmentManager = getChildFragmentManager();
                Fragment prev = fragmentManager.findFragmentByTag("dialog");
                FragmentTransaction ft = fragmentManager.beginTransaction();
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                UserInfoDialog userInfoDialog = UserInfoDialog.newInstance(post.getOwner(), uid);
                userInfoDialog.show(ft, "dialog");
            }
        }, dataBindingComponent, uid);
        adapter = new AutoClearedValue<>(this, postListAdapter);

        dataBinding.postListRecview.setAdapter(adapter.get());
        SpaceItemDecoration decoration = new SpaceItemDecoration(12);
        dataBinding.postListRecview.addItemDecoration(decoration);
        dataBinding.postListRecview.setHasFixedSize(true);
        viewModel.setPostQuery(postLoadResult);
        dataBinding.setCallback(() -> viewModel.refresh());
        dataBinding.postListSwipeRefresh.setOnRefreshListener(() -> viewModel.pullToRefresh());
    }

    private void init() {
        dataBinding.postListRecview.addOnScrollListener(new PostRecyclerViewScrollListener() {
            @Override
            public void onItemIsFirstVisibleItem(int index) {
                if (index != -1) {
//                    PostViewBindingAdapterSinglePlayer.playIndexThenPausePreviousPlayer(index);
                }
            }

            @Override
            public void onItemIsLastPosition(int index) {
                if (index == adapter.get().getItemCount() - 1) {
                    viewModel.loadPostsNextPage();
                }
            }
        });

        viewModel.getPosts().observe(getViewLifecycleOwner(), result -> {
            dataBinding.setLoadResource(result);
            int resultCount = (result == null || result.data == null) ? 0 : result.data.getContents().size();
            dataBinding.setResultCount(resultCount);
            viewModel.setPosts(result == null || result.data == null ? null : result.data.getContents());
            adapter.get().replace(result == null || result.data == null ? null : result.data.getContents());
            dataBinding.executePendingBindings();
        });

        viewModel.getLoadMoreStatus().observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                dataBinding.setLoadingMore(false);
            } else {
                dataBinding.setLoadingMore(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (state.isVerified())
                        Snackbar.make(dataBinding.postListLoadMoreBar, error, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", v -> logout()).show();
                }
            }
            dataBinding.executePendingBindings();
        });

        viewModel.getRefreshState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                dataBinding.postListSwipeRefresh.setRefreshing(false);
            } else {
                dataBinding.postListSwipeRefresh.setRefreshing(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (state.isVerified())
                        Snackbar.make(dataBinding.postListLoadMoreBar, error, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", v -> logout()).show();
                }
            }
            dataBinding.executePendingBindings();
        });

        viewModel.getPostViewModelsMap().observe(getViewLifecycleOwner(), postViewModels -> {
            adapter.get().setViewModelsMap(postViewModels);
        });

        viewModel.getProcessState().observe(getViewLifecycleOwner(), processState -> {
            if (processState == null) {
                dataBinding.setProcessState(false);
            } else {
                dataBinding.setProcessState(processState.isRunning());
                String error = processState.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (processState.isVerified())
                        Snackbar.make(dataBinding.postListLoadMoreBar, error, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", v -> logout()).show();
                }
            }
            dataBinding.executePendingBindings();
        });
    }

    public static PostListFragment newInstance(long uid, String loadPostsUrl) {
        PostListFragment fragment = new PostListFragment();
        Bundle args = new Bundle();
        args.putLong(Profile.class.getSimpleName(), uid);
        args.putString(LoadResult.class.getSimpleName(), loadPostsUrl);
        fragment.setArguments(args);
        return fragment;
    }

    private void logout() {
        viewModel.relogin();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
    }
}