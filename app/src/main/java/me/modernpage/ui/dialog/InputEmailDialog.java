package me.modernpage.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.DialogEmailInputBinding;
import me.modernpage.ui.BaseDialog;
import me.modernpage.ui.login.LoginViewModel;

public class InputEmailDialog extends BaseDialog<DialogEmailInputBinding, LoginViewModel> {
    private static final String TAG = "InputEmailDialog";

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_email_input;
    }

    @Override
    public Class<LoginViewModel> getViewModel() {
        return LoginViewModel.class;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Bundle arguments = getArguments();
        final int dialogId;
        if (arguments != null) {
            dialogId = arguments.getInt(DIALOG_ID);
            if (dialogId == 0)
                throw new IllegalArgumentException("DIALOG_ID not present in the bundle");
        } else
            throw new IllegalArgumentException("Must pass DIALOG_ID");

        dataBinding.setViewModel(viewModel);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setTitle("Reset password")
                .setMessage("Enter the email associated with your account " +
                        "and we will let you reset a new password.")
                .setView(dataBinding.getRoot());
        builder.setPositiveButton("Next", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mDialogEvent != null)
                            mDialogEvent.onNegativeDialogResult(dialogId);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        viewModel.getEmailVerifyState().observe(this, state -> {
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
                    if (dialog != null && dialog.isShowing()) {
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
                        viewModel.checkEmail(view);
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
}
