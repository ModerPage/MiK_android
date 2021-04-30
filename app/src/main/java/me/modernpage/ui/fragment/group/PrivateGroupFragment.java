package me.modernpage.ui.fragment.group;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.FragmentPrivateGroupBinding;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.ui.BaseFragment;
import me.modernpage.ui.addedit.GroupAddEditActivity;
import me.modernpage.ui.common.ItemOffsetDecoration;
import me.modernpage.ui.common.NavigationController;
import me.modernpage.ui.common.PostRecyclerViewScrollListener;
import me.modernpage.ui.login.LoginActivity;
import me.modernpage.util.AutoClearedValue;

/**
 * A simple {@link Fragment} subclass.
 */
@AndroidEntryPoint
public class PrivateGroupFragment extends BaseFragment<GroupViewModel, FragmentPrivateGroupBinding> {
    private static final String TAG = "PrivateGroupFragment";

    @Inject
    NavigationController mNavigationController;

    AutoClearedValue<PrivateGroupListAdapter> adapter;

    public PrivateGroupFragment() {
    }

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_private_group;
    }

    @Override
    public Class<GroupViewModel> getViewModel() {
        return GroupViewModel.class;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: starts");
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        long uid;
        if (args != null) {
            uid = args.getLong(Profile.class.getSimpleName());
            if (uid == 0) {
                throw new IllegalArgumentException("UID not present in bundle.");
            }
        } else {
            throw new IllegalArgumentException("UID must pass in bundle.");
        }

        initRecyclerView();

        dataBinding.setLifecycleOwner(this);
        PrivateGroupListAdapter adapter = new PrivateGroupListAdapter(group -> {
            mNavigationController.navigateToPrivateGroupDetail(group, uid);
        }, dataBindingComponent);
        dataBinding.privateGroupRecview.setAdapter(adapter);
        ItemOffsetDecoration decoration = new ItemOffsetDecoration(getContext(), R.dimen.item_offset);
        dataBinding.privateGroupRecview.addItemDecoration(decoration);

        this.adapter = new AutoClearedValue<>(this, adapter);
        dataBinding.privateGroupAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), GroupAddEditActivity.class);
                intent.putExtra(Profile.class.getSimpleName(), uid);
                startActivity(intent);
            }
        });

        dataBinding.setCallback(() -> viewModel.refresh());
        dataBinding.privateGroupSwipeRefresh.setOnRefreshListener(() -> viewModel.refresh());
    }

    private void initRecyclerView() {
        dataBinding.privateGroupRecview.addOnScrollListener(new PostRecyclerViewScrollListener() {
            @Override
            public void onItemIsFirstVisibleItem(int index) {
            }

            @Override
            public void onItemIsLastPosition(int index) {
                if (index == adapter.get().getItemCount() - 1) {
                    viewModel.loadPostsNextPage();
                }
            }
        });

        viewModel.getPrivateGroups().observe(getViewLifecycleOwner(), result -> {
            dataBinding.setLoadResource(result);
            int resultCount = (result == null || result.data == null) ? 0 : result.data.getContents().size();
            dataBinding.setResultCount(resultCount);
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
                dataBinding.privateGroupSwipeRefresh.setRefreshing(false);
            } else {
                dataBinding.privateGroupSwipeRefresh.setRefreshing(state.isRunning());
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
    }

    public static PrivateGroupFragment newInstance(long uid) {
        PrivateGroupFragment groupFragment = new PrivateGroupFragment();
        Bundle args = new Bundle();
        args.putLong(Profile.class.getSimpleName(), uid);
        groupFragment.setArguments(args);
        return groupFragment;
    }

    private void logout() {
        viewModel.relogin();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
    }
}
