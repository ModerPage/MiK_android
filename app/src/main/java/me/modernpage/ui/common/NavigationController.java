package me.modernpage.ui.common;

import android.app.Activity;
import android.content.Intent;

import androidx.fragment.app.FragmentManager;

import javax.inject.Inject;

import me.modernpage.activity.R;
import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.ui.groupdetail.PrivateGroupDetailActivity;
import me.modernpage.ui.groupdetail.PublicGroupDetailActivity;
import me.modernpage.ui.main.MainActivity;

public class NavigationController {
    private final int containerId;
    private final FragmentManager fragmentManager;
    private MainActivity mMainActivity;

    @Inject
    public NavigationController(Activity activity) {
        mMainActivity = (MainActivity) activity;
        this.containerId = R.id.main_frag_container;
        fragmentManager = mMainActivity.getSupportFragmentManager();
    }

    public void navigateToPublicGroupDetail(Group publicGroup, long uid) {
        Intent intent = new Intent(mMainActivity, PublicGroupDetailActivity.class);
        intent.putExtra(Group.class.getSimpleName(), publicGroup);
        intent.putExtra(LoadResult.class.getSimpleName(), publicGroup._posts());
        intent.putExtra(Profile.class.getSimpleName(), uid);
        mMainActivity.startActivity(intent);
    }

    public void navigateToPrivateGroupDetail(PrivateGroup privateGroup, long uid) {
        Intent intent = new Intent(mMainActivity, PrivateGroupDetailActivity.class);
        intent.putExtra(Group.class.getSimpleName(), privateGroup);
        intent.putExtra(LoadResult.class.getSimpleName(), privateGroup._posts());
        intent.putExtra(Profile.class.getSimpleName(), uid);
        mMainActivity.startActivity(intent);
    }

}
