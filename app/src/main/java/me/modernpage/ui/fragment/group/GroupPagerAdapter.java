package me.modernpage.ui.fragment.group;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class GroupPagerAdapter extends FragmentStateAdapter {
    private static final String TAG = "GroupPagerAdapter";
    private final long uid;

    public GroupPagerAdapter(@NonNull Fragment fragment, long uid) {
        super(fragment);
        this.uid = uid;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            Log.d(TAG, "getItem: position 0");
            return PublicGroupFragment.newInstance(uid);
        }
        if (position == 1) {
            Log.d(TAG, "getItem: position 1");
            return PrivateGroupFragment.newInstance(uid);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
