package me.modernpage.ui.fragment.group;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.FragmentGroupBinding;
import me.modernpage.data.local.entity.Profile;


public class GroupFragment extends Fragment {
    private static final String TAG = "GroupFragment";

    public GroupFragment() {
        // Required empty public constructor
    }

    FragmentGroupBinding dataBinding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        dataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_group, container, false);
        return dataBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
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
        dataBinding.groupPager.setAdapter(new GroupPagerAdapter(this, uid));
        new TabLayoutMediator(dataBinding.groupTab, dataBinding.groupPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) {
                    tab.setText("Public");
                } else if (position == 1) {
                    tab.setText("Private");
                }
            }
        }).attach();
    }

    public static GroupFragment newInstance(long uid) {
        GroupFragment groupFragment = new GroupFragment();
        Bundle args = new Bundle();
        args.putLong(Profile.class.getSimpleName(), uid);
        groupFragment.setArguments(args);
        return groupFragment;
    }
}


