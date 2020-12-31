package me.modernpage.fragment.group;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.activity.R;
import me.modernpage.entity.Group;

/**
 * A simple {@link Fragment} subclass.
 */
public class PublicGroupFragment extends Fragment {
    private static final String TAG = "PublicGroupFragment";
    private static final String GROUP_EXTRA = "group_extra";

    public PublicGroupFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_public_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: starts");
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = view.findViewById(R.id.public_group_recview);
        ArrayList<Group> groups = (ArrayList<Group>)getArguments().getSerializable(GROUP_EXTRA);
        GroupRecViewAdapter adapter = new GroupRecViewAdapter(groups,"public");
        recyclerView.setAdapter(adapter);

        ItemOffsetDecoration decoration = new ItemOffsetDecoration(getContext(), R.dimen.item_offset);
        recyclerView.addItemDecoration(decoration);

        Log.d(TAG, "onViewCreated: ends");
    }

    static final PublicGroupFragment newInstance(List<Group> groups) {
        PublicGroupFragment fragment = new PublicGroupFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(GROUP_EXTRA,(ArrayList<Group>) groups);
        fragment.setArguments(bundle);
        return fragment;
    }
}
