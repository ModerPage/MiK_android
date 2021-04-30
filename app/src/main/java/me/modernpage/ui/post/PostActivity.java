package me.modernpage.ui.post;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivityPostBinding;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.ui.BaseActivity;
import me.modernpage.ui.fragment.postlist.PostListFragment;

@AndroidEntryPoint
public class PostActivity extends BaseActivity<ActivityPostBinding> {

    @Override
    public int getLayoutRes() {
        return R.layout.activity_post;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) dataBinding.postToolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle args = getIntent().getExtras();
        final long uid;
        if (args != null) {
            uid = args.getLong(Profile.class.getSimpleName());
            if (uid == 0) {
                throw new IllegalArgumentException("UID not present in bundle");
            }
        } else {
            throw new IllegalArgumentException("UID must be passed in bundle");
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(dataBinding.postContainer.getId(),
                PostListFragment.class, args).commit();
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