package me.modernpage.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.DialogResetPasswordBinding;
import me.modernpage.databinding.ChangePasswordAdapter;
import me.modernpage.ui.BaseDialog;
import me.modernpage.ui.login.LoginViewModel;
import me.modernpage.util.Constants;

public class ResetPasswordDialog extends BaseDialog<DialogResetPasswordBinding, LoginViewModel> implements
        ChangePasswordAdapter.NewAfterTextChanged, ChangePasswordAdapter.ConfirmOnTextChanged {
    @Override
    public int getLayoutRes() {
        return R.layout.dialog_reset_password;
    }

    @Override
    public Class<LoginViewModel> getViewModel() {
        return LoginViewModel.class;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dataBinding.setViewModel(viewModel);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final Bundle arguments = getArguments();
        final int dialogId;
        if (arguments != null) {
            dialogId = arguments.getInt(DIALOG_ID);
            if (dialogId == 0)
                throw new IllegalArgumentException("DIALOG_ID not present in the bundle");
        } else
            throw new IllegalArgumentException("Must pass DIALOG_ID");

        builder.setTitle("Create new password")
                .setMessage("Your new password must be different from previous used password.")
                .setView(dataBinding.getRoot());

        builder.setPositiveButton("Save", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mDialogEvent.onNegativeDialogResult(dialogId);
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dataBinding.setNewAfterTextChanged(this);
        dataBinding.setConfirmOnTextChanged(this);

        viewModel.getResetPasswordState().observe(this, state -> {
            if (state == null) {
                dataBinding.setProcessState(false);
            } else {
                dataBinding.setProcessState(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                }
                Boolean data = (Boolean) state.getData();
                if (data != null && data) {
                    if (dialog.isShowing()) {
                        mDialogEvent.onPositiveDialogResult(dialogId);
                        dialog.dismiss();
                    }
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
                        viewModel.resetPasswordClick(view);
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
    public void confirmOnTextChanged(CharSequence s, int start, int before, int count) {
        String newPassword = dataBinding.resetPasswordNewPassword.getText().toString();
        if (newPassword != null && !newPassword.contentEquals(s)) {
            dataBinding.resetPasswordConfirmPassword
                    .setError("Passwords do not match.");
        }
        viewModel.setConfirmPassword(s.toString());
    }

    @Override
    public void newAfterTextChanged(Editable s) {
        if (s != null && s.toString().trim().length() > 0) {
            if (isNotValid(s.toString())) {
                dataBinding.resetPasswordNewPassword
                        .setError("Password must be 8~20 characters long and contain a digit.");
            }

            String confirmPassword = dataBinding.resetPasswordConfirmPassword.getText().toString();
            if (confirmPassword != null && confirmPassword.equals(s.toString())) {
                dataBinding.resetPasswordConfirmPassword.setError(null);
            } else {
                dataBinding.resetPasswordConfirmPassword.setError("Passwords do not match.");
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
