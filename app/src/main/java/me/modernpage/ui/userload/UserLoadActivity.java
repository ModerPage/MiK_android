package me.modernpage.ui.userload;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivityUserLoadBinding;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.ui.BaseActivity;
import me.modernpage.ui.common.PostRecyclerViewScrollListener;
import me.modernpage.ui.login.LoginActivity;
import me.modernpage.util.Status;

@AndroidEntryPoint
public class UserLoadActivity extends BaseActivity<ActivityUserLoadBinding> {
    public static final String EXTRA_TITLE = "title";
    UserLoadViewModel viewModel;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_user_load;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getIntent().getExtras();
        final long uid;
        final String loadResult;
        final String title;
        if (args != null) {
            uid = args.getLong(Profile.class.getSimpleName());
            loadResult = args.getString(LoadResult.class.getSimpleName());
            title = args.getString(UserLoadActivity.EXTRA_TITLE);
            if (uid == 0 || loadResult == null || title == null) {
                throw new IllegalArgumentException("UID and/or LOAD RESULT and/or title not present in bundle");
            }
        } else {
            throw new IllegalArgumentException("UID and/or LOAD RESULT and/or title must passed in bundle");
        }

        Toolbar toolbar = (Toolbar) dataBinding.followerFollowingToolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        dataBinding.setTitle(title);

        viewModel = new ViewModelProvider(this).get(UserLoadViewModel.class);
        DataListAdapter adapter = new DataListAdapter(dataBindingComponent);
        dataBinding.followerFollowingFollowerList.setAdapter(adapter);
        dataBinding.setCallback(() -> viewModel.refresh());
        dataBinding.followerFollowingSwipeRefresh.setOnRefreshListener(() -> viewModel.pullToRefresh());

        dataBinding.followerFollowingFollowerList.addOnScrollListener(new PostRecyclerViewScrollListener() {
            @Override
            public void onItemIsFirstVisibleItem(int index) {
            }

            @Override
            public void onItemIsLastPosition(int index) {
                if (index == adapter.getItemCount() - 1) {
                    viewModel.loadFollowersNextPage();
                }
            }
        });

        viewModel.setLoadQuery(loadResult);
        viewModel.getData().observe(this, result -> {
            if (result.status == Status.LOGOUT) {
                Snackbar.make(dataBinding.getRoot(), result.message, Snackbar.LENGTH_INDEFINITE)
                        .setAction("LOGOUT", view -> logout()).show();
                return;
            }
            dataBinding.setLoadResource(result);
            int resultCount = result.data == null ? 0 : result.data.getContents().size();
            dataBinding.setResultCount(resultCount);
            adapter.replace(result.data == null ? null : result.data.getContents());
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
                dataBinding.followerFollowingSwipeRefresh.setRefreshing(false);
            } else {
                dataBinding.followerFollowingSwipeRefresh.setRefreshing(state.isRunning());
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
    }

    private void logout() {
        viewModel.relogin();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}