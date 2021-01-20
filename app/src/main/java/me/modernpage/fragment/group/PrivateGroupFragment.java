package me.modernpage.fragment.group;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.Constants;
import me.modernpage.activity.R;
import me.modernpage.entity.Group;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrivateGroupFragment extends Fragment {
    private static final String TAG = "PrivateGroupFragment";
    public PrivateGroupFragment(){}

    private List<Group> mGroups;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_private_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: starts");
        super.onViewCreated(view, savedInstanceState);

        if (null != getArguments()) {
            mGroups = (ArrayList<Group>) getArguments().getSerializable(Constants.User.GROUP_EXTRA);
            GroupRecViewAdapter adapter = new GroupRecViewAdapter(mGroups, "private");
            RecyclerView recyclerView = view.findViewById(R.id.private_group_recview);
            recyclerView.setAdapter(adapter);

            ItemOffsetDecoration decoration = new ItemOffsetDecoration(getContext(), R.dimen.item_offset);
            recyclerView.addItemDecoration(decoration);
        }
        Log.d(TAG, "onViewCreated: ends");
    }

    static final PrivateGroupFragment newInstance(List<Group> groups) {
        PrivateGroupFragment fragment = new PrivateGroupFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.User.GROUP_EXTRA, (ArrayList<Group>) groups);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: called");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: called");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: called");
    }
}
