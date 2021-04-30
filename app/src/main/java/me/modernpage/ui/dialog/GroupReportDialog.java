package me.modernpage.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.DialogReportBinding;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.data.local.entity.Profile;

public class GroupReportDialog extends DialogFragment {

    private DialogReportBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_report,
                null, false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        final Bundle args = requireArguments();
        final long userId = args.getLong(Profile.class.getSimpleName());
        final long groupId = args.getLong(PrivateGroup.class.getSimpleName());
        if (groupId == 0 || userId == 0) {
            throw new IllegalArgumentException("GROUP ID and/or USER ID not present in the bundle.");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Report")
                .setMessage("If someone is in immediate danger, get help before reporting to MiK. Don't wait.")
                .setView(binding.getRoot())
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
                    String text = binding.reportText.getText().toString();
                    if (text.trim().length() == 0) {
                        binding.reportText.setError("Title can't be blank.");
                        return;
                    }

                    long groupId = requireArguments().getLong(PrivateGroup.class.getSimpleName());
                    long userId = requireArguments().getLong(Profile.class.getSimpleName());

                    @SuppressLint("DefaultLocale") String title = String.
                            format("Group with %d id has reported by user with %d id", groupId, userId);

                    Uri uri = Uri.parse("mailto:")
                            .buildUpon()
                            .appendQueryParameter("subject", title)
                            .appendQueryParameter("body", text)
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

    public static GroupReportDialog newInstance(Long groupId, Long userId) {
        Bundle args = new Bundle();
        args.putLong(PrivateGroup.class.getSimpleName(), groupId);
        args.putLong(Profile.class.getSimpleName(), userId);
        GroupReportDialog dialog = new GroupReportDialog();
        dialog.setArguments(args);
        return dialog;
    }
}
