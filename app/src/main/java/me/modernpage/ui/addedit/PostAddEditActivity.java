package me.modernpage.ui.addedit;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivityPostAddEditBinding;
import me.modernpage.ui.BaseActivity;
import me.modernpage.ui.dialog.AppDialog;
import me.modernpage.ui.fragment.post.PostFragment;

@AndroidEntryPoint
public class PostAddEditActivity extends BaseActivity<ActivityPostAddEditBinding>
        implements AppDialog.DialogEvents, PostFragment.OnSaveClicked {
    private static final String TAG = "PostAddEditActivity";
    private static final int DIALOG_ID_CANCEL_EDIT = 1;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_post_add_edit;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = (Toolbar) dataBinding.postAddEditToolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle arguments = getIntent().getExtras();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(dataBinding.postAddEditContainer.getId(),
                PostFragment.class, arguments).commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            PostFragment postFragment = (PostFragment) getSupportFragmentManager()
                    .findFragmentById(dataBinding.postAddEditContainer.getId());
            if (postFragment.canClose()) {
                finish(false);
            } else {
                showConfirmationDialog();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    public void onPositiveDialogResult(int dialogId, Bundle args) {

    }

    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {
        finish(false);
    }

    @Override
    public void onDialogCancelled(int dialogId) {

    }

    @Override
    public void onSaveClicked() {
        finish(true);
    }

    private void finish(boolean result) {
        Intent data = new Intent();
        data.putExtra("result", result);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onBackPressed() {
        PostFragment postFragment = (PostFragment) getSupportFragmentManager()
                .findFragmentById(dataBinding.postAddEditContainer.getId());
        if (postFragment.canClose())
            super.onBackPressed();
        else
            showConfirmationDialog();
    }
}