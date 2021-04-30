package me.modernpage.ui.dialog;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.DialogUserInfoBinding;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.ui.BaseDialog;
import me.modernpage.util.ImagePopUpHelper;

@AndroidEntryPoint
public class UserInfoDialog extends BaseDialog<DialogUserInfoBinding, UserInfoViewModel> {

    private static final String TAG = "UserInfoDialog";

    public static final String UID = "uid";

    @Inject
    ImagePopUpHelper mImagePopUpHelper;

    private Profile mProfile;
    private long uid;

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_user_info;
    }

    @Override
    public Class<UserInfoViewModel> getViewModel() {
        return UserInfoViewModel.class;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = requireArguments();
        mProfile = (Profile) args.getSerializable(Profile.class.getSimpleName());
        uid = args.getLong(UID);
        if (mProfile == null || uid == 0) {
            throw new IllegalArgumentException("Profile data and/or uid must passed in bundle.");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return dataBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dataBinding.setProfile(mProfile);
        mImagePopUpHelper.enablePopUpOnClick(getActivity(), dataBinding.userInfoAvatar);
        viewModel.getPosts(mProfile._posts()).observe(this, posts -> {
            dataBinding.setPosts(posts == null ? null : posts.data);
            dataBinding.executePendingBindings();
        });

        viewModel.getFollowing(mProfile._following()).observe(this, following -> {
            dataBinding.setFollowing(following == null ? null : following.data);
            dataBinding.executePendingBindings();
        });

        viewModel.getFollowers(mProfile._followers(), uid).observe(this, followers -> {
            Log.d(TAG, "onViewCreated: followers: " + followers);
            dataBinding.setFollowers(followers == null ? null : followers.data);
            dataBinding.executePendingBindings();
        });

        dataBinding.userInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewModel.followControl(mProfile._followers(), mProfile.getId(), uid);
            }
        });

        viewModel.followControlState().observe(this, state -> {
            if (state == null) {
                dataBinding.setProcessState(false);
            } else {
                dataBinding.setProcessState(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                }
            }
            dataBinding.executePendingBindings();
        });
    }

    public static UserInfoDialog newInstance(Profile profile, long uid) {
        Bundle args = new Bundle();
        args.putSerializable(Profile.class.getSimpleName(), profile);
        args.putLong(UID, uid);
        UserInfoDialog userInfoDialog = new UserInfoDialog();
        userInfoDialog.setArguments(args);
        return userInfoDialog;
    }
}
