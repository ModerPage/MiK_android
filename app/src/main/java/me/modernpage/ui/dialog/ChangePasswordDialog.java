package me.modernpage.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.common.base.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.DialogChangePasswordBinding;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.databinding.ChangePasswordAdapter;
import me.modernpage.ui.BaseDialog;
import me.modernpage.ui.settings.SettingsViewModel;
import me.modernpage.util.Constants;

public class ChangePasswordDialog extends BaseDialog<DialogChangePasswordBinding, SettingsViewModel>
        implements ChangePasswordAdapter.CurrentOnTextChanged,
        ChangePasswordAdapter.ConfirmOnTextChanged,
        ChangePasswordAdapter.NewAfterTextChanged {
    private static final String TAG = "ChangePasswordDialog";

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_change_password;
    }

    @Override
    public Class<SettingsViewModel> getViewModel() {
        return SettingsViewModel.class;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: called");
        dataBinding.setViewModel(viewModel);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final Bundle arguments = getArguments();
        final int dialogId;
        final long profileId;
        if (arguments != null) {
            dialogId = arguments.getInt(DIALOG_ID);
            profileId = arguments.getLong(Profile.class.getSimpleName());
            if (dialogId == 0 || profileId == 0)
                throw new IllegalArgumentException("DIALOG_ID and/or PROFILE_ID not present in the bundle");
        } else
            throw new IllegalArgumentException("Must pass DIALOG_ID and PROFILE_ID");

        dataBinding.setCurrentOnTextChanged(this);
        dataBinding.setConfirmOnTextChanged(this);
        dataBinding.setNewAfterTextChanged(this);

        builder.setTitle("Create new password")
                .setMessage("Your new password must be different from previous used password.")
                .setView(dataBinding.getRoot());

        builder.setPositiveButton("Confirm", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mDialogEvent.onNegativeDialogResult(dialogId);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        viewModel.getPasswordCheckState().observe(this, state -> {
            if (state == null) {
                dataBinding.setCurrentPasswordState(false);
            } else {
                dataBinding.setCurrentPasswordState(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (state.isVerified())
                        dataBinding.changePasswordCurrent.setError(error);
                    else
                        Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                }
                Boolean data = state.getData();
                if (data != null && data) {
                    dataBinding.changePasswordCurrent.setBackground(
                            ContextCompat.getDrawable(getContext(), R.drawable.textfield_correct_bg));
                }
            }
            dataBinding.executePendingBindings();
        });
        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        AlertDialog alertDialog = (AlertDialog) getDialog();
        if (alertDialog != null) {
            Button positiveButton = alertDialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mDialogEvent != null) {
                        if (viewModel.confirmChangePassword()) {
                            int dialogId = getArguments().getInt(DIALOG_ID);
                            mDialogEvent.onPositiveDialogResult(dialogId);
                            dismiss();
                        }
                    }
                }
            });
        }
    }


    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        if (mDialogEvent != null) {
            int dialogId = getArguments().getInt(DIALOG_ID);
            mDialogEvent.onNegativeDialogResult(dialogId);
        }
    }


    @Override
    public void currentOnTextChanged(CharSequence s, int start, int before, int count) {
        dataBinding.changePasswordCurrent.setBackground(
                ContextCompat.getDrawable(getContext(), R.drawable.textfield_bg));
        if (isNotValid(s.toString())) {
            dataBinding.changePasswordCurrent
                    .setError("Password must be 8~20 characters long and contain a digit.");
            return;
        }
        viewModel.checkPassword(s.toString());
        String newPassword = dataBinding.changePasswordNewpassword.getText().toString();
        if (newPassword != null && newPassword.contentEquals(s)) {
            dataBinding.changePasswordNewpassword.setError("Password must differ from old password.");
        }

        viewModel.setCurrentPassword(s.toString());
    }

    @Override
    public void confirmOnTextChanged(CharSequence s, int start, int before, int count) {
        String newPassword = dataBinding.changePasswordNewpassword.getText().toString();
        if (newPassword != null && !newPassword.contentEquals(s)) {
            dataBinding.changePasswordConfirmpassword
                    .setError("Passwords do not match.");
        }
        viewModel.setConfirmPassword(s.toString());
    }

    @Override
    public void newAfterTextChanged(Editable s) {
        if (s != null && s.toString().trim().length() > 0) {
            Log.d(TAG, "newAfterTextChanged: called");
            if (isNotValid(s.toString())) {
                dataBinding.changePasswordNewpassword
                        .setError("Password must be 8~20 characters long and contain a digit.");
                return;
            }

            String currentPassword = viewModel.getCurrentPassword().getValue();
            if (currentPassword != null && currentPassword.equals(s.toString())) {
                dataBinding.changePasswordNewpassword
                        .setError("Password must differ from old password.");
                return;
            }

            String confirmPassword = dataBinding.changePasswordConfirmpassword.getText().toString();
            if (!Strings.isNullOrEmpty(confirmPassword)) {
                dataBinding.changePasswordConfirmpassword.setError(
                        confirmPassword.equals(s.toString()) ? null : "Passwords do not match.");
            }
            viewModel.setNewPassword(s.toString());
        }
    }

    private boolean isNotValid(String field) {
        Pattern pattern = Pattern.compile(Constants.Regex.PASSWORD);
        Matcher matcher = pattern.matcher(field);
        return !matcher.matches();
    }
}
