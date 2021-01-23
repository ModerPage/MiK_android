package me.modernpage.fragment.group;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.util.Constants;
import me.modernpage.activity.R;
import me.modernpage.entity.Group;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment {
    private static final String TAG = "GroupFragment";
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private List<Group> mGroups;
    private GroupPagerAdapter mPagerAdapter;
    public GroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: starts");
        mViewPager = view.findViewById(R.id.group_pager);
        mTabLayout = view.findViewById(R.id.group_tab);

        if (null != getArguments()) {
            mGroups = (ArrayList<Group>) getArguments().getSerializable(Constants.User.GROUP_EXTRA);
            mPagerAdapter = new GroupPagerAdapter(getChildFragmentManager(), mGroups);
            mViewPager.setAdapter(mPagerAdapter);
            mTabLayout.setupWithViewPager(mViewPager);
        }
        Log.d(TAG, "onViewCreated: ends");
    }


    public static Fragment newInstance(List<Group> currentGroups) {
        GroupFragment fragment = new GroupFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.User.GROUP_EXTRA, (ArrayList<Group>) currentGroups);
        fragment.setArguments(bundle);
        return fragment;
    }
}


