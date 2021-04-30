package me.modernpage.ui.fragment.group;


import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.FragmentPublicGroupBinding;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.ui.BaseFragment;
import me.modernpage.ui.common.ItemOffsetDecoration;
import me.modernpage.ui.common.NavigationController;
import me.modernpage.util.AutoClearedValue;

/**
 * A simple {@link Fragment} subclass.
 */
@AndroidEntryPoint
public class PublicGroupFragment extends BaseFragment<GroupViewModel, FragmentPublicGroupBinding> {
    private static final String TAG = "PublicGroupFragment";

    @Inject
    NavigationController mNavigationController;

    AutoClearedValue<PublicGroupListAdapter> adapter;

    public PublicGroupFragment() {
    }

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_public_group;
    }

    @Override
    public Class<GroupViewModel> getViewModel() {
        return GroupViewModel.class;
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

        initRecyclerView();

        PublicGroupListAdapter groupListAdapter = new PublicGroupListAdapter(group -> {
            Log.d(TAG, "onViewCreated: called group click");
            mNavigationController.navigateToPublicGroupDetail(group, uid);
        }, dataBindingComponent);

        dataBinding.publicGroupRecview.setAdapter(groupListAdapter);
        ItemOffsetDecoration decoration = new ItemOffsetDecoration(getContext(), R.dimen.item_offset);
        dataBinding.publicGroupRecview.addItemDecoration(decoration);
        adapter = new AutoClearedValue<>(this, groupListAdapter);

    }

    private void initRecyclerView() {
        viewModel.getPublicGroups().observe(getViewLifecycleOwner(), listResource -> {
            adapter.get().replace(listResource == null ? null : listResource.data);
            dataBinding.executePendingBindings();
        });
    }

    public static PublicGroupFragment newInstance(long uid) {
        PublicGroupFragment groupFragment = new PublicGroupFragment();
        Bundle args = new Bundle();
        args.putLong(Profile.class.getSimpleName(), uid);
        groupFragment.setArguments(args);
        return groupFragment;
    }
}
