package me.modernpage.fragment.group;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

import me.modernpage.entity.Group;

public class GroupPagerAdapter extends FragmentPagerAdapter {
    private static final String TAG = "GroupPagerAdapter";
    private List<Group> mGroups;

    public GroupPagerAdapter(@NonNull FragmentManager fm, List<Group> groups) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        Log.d(TAG, "GroupPagerAdapter: called");
        mGroups = groups;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(position == 0) {
            Log.d(TAG, "getItem: position 0");
            return PublicGroupFragment.newInstance(mGroups);
        }
        if(position == 1) {
            Log.d(TAG, "getItem: position 1");
            return PrivateGroupFragment.newInstance(mGroups);
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0)
            return "Public";
        if(position == 1)
            return "Private";
        return super.getPageTitle(position);
    }
}
