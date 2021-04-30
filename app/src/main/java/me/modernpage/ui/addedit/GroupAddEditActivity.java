package me.modernpage.ui.addedit;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivityGroupAddEditBinding;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.ui.BaseActivity;
import me.modernpage.ui.dialog.AppDialog;
import me.modernpage.ui.fragment.group.CreateGroupFragment;

@AndroidEntryPoint
public class GroupAddEditActivity extends BaseActivity<ActivityGroupAddEditBinding> implements AppDialog.DialogEvents {
    public static final String EXTRA_URL = "url";
    private static final int DIALOG_ID_CANCEL_EDIT = 100;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_group_add_edit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getIntent().getExtras();
        PrivateGroup privateGroup = null;
        if (args != null) {
            privateGroup = (PrivateGroup) args.getSerializable(PrivateGroup.class.getSimpleName());
        }

        Toolbar toolbar = (Toolbar) dataBinding.groupAddEditToolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            if (privateGroup == null) {
                dataBinding.groupAddEditToolbarText.setText("New Group");
                fragmentManager.beginTransaction().replace(dataBinding.groupAddEditContainer.getId(),
                        CreateGroupFragment.class, args).commit();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (canClose())
                finish();
            else
                showConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean canClose() {
        return false;
    }

    private void showConfirmationDialog() {
        AppDialog dialog = new AppDialog();
        Bundle args = new Bundle();
        args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_CANCEL_EDIT);
        args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.cancelEditDiag_message));
        args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.cancelEditDiag_positive_caption);
        args.putInt(AppDialog.DIALOG_NEGATIVE_RID, R.string.cancelEditDiag_negative_caption);

        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onBackPressed() {
        if (canClose())
            super.onBackPressed();
        else
            showConfirmationDialog();
    }

    @Override
    public void onPositiveDialogResult(int dialogId, Bundle args) {

    }

    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {
        finish();
    }

    @Override
    public void onDialogCancelled(int dialogId) {

    }
}