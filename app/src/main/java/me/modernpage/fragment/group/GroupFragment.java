package me.modernpage.fragment.group;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;

import java.util.List;

import me.modernpage.activity.R;
import me.modernpage.entity.Group;
import me.modernpage.task.GetAllGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupFragment extends Fragment implements GetAllGroup.OnGetAllGroup {
    private static final String TAG = "GroupFragment";
    private ViewPager mViewPager;
    private TabLayout mTabLayout;

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
        GetAllGroup getAllGroup = new GetAllGroup(getContext(), this);
        getAllGroup.execute();
        Log.d(TAG, "onViewCreated: ends");
    }

    @Override
    public void onGetAllGroupComplete(List<Group> groups) {
        GroupPagerAdapter pagerAdapter = new GroupPagerAdapter(getParentFragmentManager(), groups);
        mViewPager.setAdapter(pagerAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
    }

}


