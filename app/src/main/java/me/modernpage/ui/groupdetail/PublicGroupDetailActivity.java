package me.modernpage.ui.groupdetail;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivityPublicGroupDetailBinding;
import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.ui.BaseActivity;
import me.modernpage.ui.addedit.PostAddEditActivity;
import me.modernpage.ui.fragment.post.PostFragment;
import me.modernpage.ui.fragment.postlist.PostListFragment;

@AndroidEntryPoint
public class PublicGroupDetailActivity extends BaseActivity<ActivityPublicGroupDetailBinding> {
    private static final String TAG = "GroupDetailActivity";
    GroupDetailViewModel viewModel;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_public_group_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getIntent().getExtras();
        final long uid;
        final Group group;
        if (args != null) {
            uid = args.getLong(Profile.class.getSimpleName());
            group = (Group) args.getSerializable(Group.class.getSimpleName());
            if (uid == 0 || group == null)
                throw new IllegalArgumentException("UID and/or Group not present in bundle.");
        } else {
            throw new IllegalArgumentException("UID and Group must be passed in bundle.");
        }
        viewModel = new ViewModelProvider(this).get(GroupDetailViewModel.class);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putLong(Profile.class.getSimpleName(), uid);
        bundle.putString(LoadResult.class.getSimpleName(), group._posts());
        fragmentManager.beginTransaction().replace(dataBinding.publicGroupDetailContainer.getId(),
                PostListFragment.class, bundle).commit();

        Toolbar toolbar = dataBinding.publicGroupDetailToolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        dataBinding.setGroup(group);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.group_detail_menu_report).setVisible(false);
        menu.findItem(R.id.group_detail_menu_leave).setVisible(false);
        menu.findItem(R.id.group_detail_menu_del_leave).setVisible(false);
        menu.findItem(R.id.group_detail_menu_create_post).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.group_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.group_detail_menu_create_post:
                Group group = dataBinding.getGroup();
                if (group != null) {
                    Bundle args = getIntent().getExtras();
                    args.putSerializable(PostFragment.EXTRA_GROUP, group);
                    Intent intent = new Intent(PublicGroupDetailActivity.this, PostAddEditActivity.class);
                    intent.putExtras(args);
                    startActivity(intent);
                }
                break;
        }
        return true;
    }
}