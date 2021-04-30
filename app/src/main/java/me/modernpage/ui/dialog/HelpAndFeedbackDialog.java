package me.modernpage.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.DialogHelpandfeedbackBinding;

public class HelpAndFeedbackDialog extends DialogFragment {

    private DialogHelpandfeedbackBinding mDataBinding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mDataBinding = DataBindingUtil.inflate(((Activity) context).getLayoutInflater(), R.layout.dialog_helpandfeedback, null, false);
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Help & Feedback")
                .setMessage("Report any issue you have faced, feel free to contact us, we will get back to you as soon as we can!")
                .setView(mDataBinding.getRoot())
                .setPositiveButton("Submit", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // cancelling dialog
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        AlertDialog alertDialog = (AlertDialog) getDialog();
        if (alertDialog != null) {
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String title = mDataBinding.helpfeedbackTitle.getText().toString();
                    String description = mDataBinding.helpfeedbackDescription.getText().toString();
                    if (title.trim().length() == 0) {
                        mDataBinding.helpfeedbackTitle.setError("Title can't be blank.");
                        return;
                    }
                    if (description.trim().length() == 0) {
                        mDataBinding.helpfeedbackDescription.setError("Description can't be blank.");
                        return;
                    }

                    Uri uri = Uri.parse("mailto:")
                            .buildUpon()
                            .appendQueryParameter("subject", title)
                            .appendQueryParameter("body", description)
                            .build();

                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"test@mail.com"});
                    if (intent.resolveActivity(getActivity().getPackageManager()) != null)
                        startActivity(intent);
                    dismiss();
                }
            });
        }
    }
}
