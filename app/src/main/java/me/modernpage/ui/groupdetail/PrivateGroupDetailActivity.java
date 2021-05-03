package me.modernpage.ui.groupdetail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivityPrivateGroupDetailBinding;
import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.ui.BaseActivity;
import me.modernpage.ui.addedit.PostAddEditActivity;
import me.modernpage.ui.dialog.AppDialog;
import me.modernpage.ui.dialog.PostReportDialog;
import me.modernpage.ui.fragment.post.PostFragment;
import me.modernpage.ui.fragment.postlist.PostListFragment;
import me.modernpage.ui.login.LoginActivity;
import me.modernpage.util.Status;

@AndroidEntryPoint
public class PrivateGroupDetailActivity extends BaseActivity<ActivityPrivateGroupDetailBinding> implements
        AppDialog.DialogEvents {
    private static final String TAG = "PrivateGroupDetailActiv";
    private static final int DIALOG_ID_JOIN_GROUP = 101;
    private static final int DIALOG_ID_DELETE_LEAVE = 102;
    private static final int DIALOG_ID_LEAVE_ID = 103;
    GroupDetailViewModel viewModel;
    private PrivateGroup group;
    private long uid;
    private Menu mMenu;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_private_group_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getIntent().getExtras();
        if (args != null) {
            uid = args.getLong(Profile.class.getSimpleName());
            group = (PrivateGroup) args.getSerializable(Group.class.getSimpleName());
            if (uid == 0 || group == null)
                throw new IllegalArgumentException("UID and/or Group not present in bundle.");
        } else {
            throw new IllegalArgumentException("UID and Group must be passed in bundle.");
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Bundle bundle = new Bundle();
        bundle.putLong(Profile.class.getSimpleName(), uid);
        bundle.putString(LoadResult.class.getSimpleName(), group._posts());
        fragmentManager.beginTransaction().replace(dataBinding.privateGroupDetailContainer.getId(),
                PostListFragment.class, bundle).commit();

        Toolbar toolbar = dataBinding.privateGroupDetailToolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        viewModel = new ViewModelProvider(this).get(GroupDetailViewModel.class);
        dataBinding.setGroup(group);

        viewModel.getMembers(group._members(), uid).observe(this, members -> {
            if (members.status == Status.SUCCESS) {
                if (members.data != null) {
                    Log.d(TAG, "onCreate: joined: " + members.data.getJoined());
                    dataBinding.setMembersCount(members.data.getTotal());
                    if (members.data.getJoined() != null && !members.data.getJoined()) {
                        Fragment prev = fragmentManager.findFragmentByTag("joinGroupDialog");
                        FragmentTransaction ft = fragmentManager.beginTransaction();
                        if (prev != null) {
                            ft.remove(prev);
                        }
                        ft.addToBackStack(null);
                        AppDialog appDialog = new AppDialog();
                        Bundle dialogBundle = new Bundle();
                        dialogBundle.putInt(AppDialog.DIALOG_ID, DIALOG_ID_JOIN_GROUP);
                        dialogBundle.putString(AppDialog.DIALOG_MESSAGE, "You are not a member of this group");
                        dialogBundle.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.JoinGroupDiag_Positive_Caption);
                        dialogBundle.putInt(AppDialog.DIALOG_NEGATIVE_RID, R.string.JoinGroupDiag_Negative_Caption);
                        appDialog.setArguments(dialogBundle);
                        appDialog.show(ft, "joinGroupDialog");
                    }
                }
            } else if (members.status == Status.LOGOUT) {
                Snackbar.make(dataBinding.getRoot(), members.message, Snackbar.LENGTH_INDEFINITE)
                        .setAction("LOGOUT", view -> logout()).show();
            } else if (members.status == Status.ERROR) {
                Snackbar.make(dataBinding.getRoot(), members.message, Snackbar.LENGTH_LONG).show();
            }
            dataBinding.executePendingBindings();
        });

        viewModel.deleteLeaveState().observe(this, state -> {
            if (state == null) {
                dataBinding.setProcessState(false);
            } else {
                dataBinding.setProcessState(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (state.isVerified())
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", view -> logout()).show();
                }
                Boolean data = state.getData();
                if (data != null && data) {
                    finish();
                }
            }
            dataBinding.executePendingBindings();
        });


        viewModel.joinState().observe(this, state -> {
            if (state == null) {
                dataBinding.setProcessState(false);
            } else {
                dataBinding.setProcessState(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();

                if (error != null) {
                    if (state.isVerified())
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", view -> logout()).show();
                }
            }
        });
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu: called");
        if (uid == group.getOwnerId()) {
            menu.findItem(R.id.group_detail_menu_report).setVisible(false);
            menu.findItem(R.id.group_detail_menu_leave).setVisible(false);
        } else {
            menu.findItem(R.id.group_detail_menu_del_leave).setVisible(false);
        }
        menu.findItem(R.id.group_detail_menu_create_post).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: called");
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.group_detail_menu, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.group_detail_menu_create_post:
                if (group != null) {
                    Bundle args = getIntent().getExtras();
                    args.putSerializable(PostFragment.EXTRA_GROUP, group);
                    Intent intent = new Intent(PrivateGroupDetailActivity.this, PostAddEditActivity.class);
                    intent.putExtras(args);
                    startActivity(intent);
                }
                break;
            case R.id.group_detail_menu_del_leave:
                Fragment prev = getSupportFragmentManager().findFragmentByTag("deleteAndLeaveDialog");
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                if (prev != null) {
                    ft.remove(prev);
                }
                AppDialog dialog = new AppDialog();
                Bundle args = new Bundle();
                args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_DELETE_LEAVE);
                args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.deleavediag_message));
                args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.deldiag_positive_caption);
                dialog.setArguments(args);
                dialog.show(ft, "deleteAndLeaveDialog");
                break;
            case R.id.group_detail_menu_leave:
                prev = getSupportFragmentManager().findFragmentByTag("leaveDialog");
                ft = getSupportFragmentManager().beginTransaction();
                if (prev != null) {
                    ft.remove(prev);
                }
                dialog = new AppDialog();
                args = new Bundle();
                args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_LEAVE_ID);
                args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.leavediag_message));
                args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.leavediag_positive_caption);
                dialog.setArguments(args);
                dialog.show(ft, "leaveDialog");
                break;
            case R.id.group_detail_menu_report:
                if (group != null) {
                    prev = getSupportFragmentManager().findFragmentByTag("groupReportDialog");
                    ft = getSupportFragmentManager().beginTransaction();
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    PostReportDialog postReportDialog = PostReportDialog.newInstance(group.getId(), uid);
                    postReportDialog.show(ft, "groupReportDialog");
                }
        }
        return true;
    }

    private void logout() {
        viewModel.relogin();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPositiveDialogResult(int dialogId, Bundle args) {
        if (group == null) throw new AssertionError("Group data is null");

        switch (dialogId) {
            case DIALOG_ID_JOIN_GROUP:
                viewModel.addMember(group._members(), group.getId(), uid);
                break;
            case DIALOG_ID_DELETE_LEAVE:
                viewModel.deleteGroup(group);
                break;
            case DIALOG_ID_LEAVE_ID:
                viewModel.leaveGroup(group._members(), group.getId(), uid);
                break;
        }
    }

    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {
        if (dialogId == DIALOG_ID_JOIN_GROUP)
            finish();
    }

    @Override
    public void onDialogCancelled(int dialogId) {
        finish();
    }
}