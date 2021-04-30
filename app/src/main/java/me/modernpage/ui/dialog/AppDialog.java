package me.modernpage.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import me.modernpage.activity.R;

public class AppDialog extends AppCompatDialogFragment {
    private static final String TAG = "AppDialog";

    public static final String DIALOG_ID = "id";
    public static final String DIALOG_MESSAGE = "message";
    public static final String DIALOG_POSITIVE_RID = "positive_rid";
    public static final String DIALOG_NEGATIVE_RID = "negative_rid";

    /**
     * dialogId is used to define which dialog is being called, since app has
     * multiple different dialogs, will be given unique id ,
     * same mechanism as loaderManager.initLoader() and for menu items
     */
    public interface DialogEvents {
        void onPositiveDialogResult(int dialogId, Bundle args);

        void onNegativeDialogResult(int dialogId, Bundle args);

        void onDialogCancelled(int dialogId);
    }

    private DialogEvents mDialogEvents;

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: called");
        super.onAttach(context);

        // Activities containing this fragment must implement its callbacks
        if (!(context instanceof DialogEvents)) {
            throw new ClassCastException(context.toString() + " must implement AppDialog.DialogEvents interface");
        }

        mDialogEvents = (DialogEvents) context;
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: called");
        super.onDetach();

        // Reset the active callbacks interface, because we don't have an activity any longer
        mDialogEvents = null;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: called");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final Bundle arguments = getArguments();
        final int dialogId;
        String messageString;
        int positiveStringId;
        int negativeStringId;

        // dialog id and messageString are critical data for this fragment
        // we can't proceed to create the dialog unless they are passed
        if (arguments != null) {
            dialogId = arguments.getInt(DIALOG_ID);
            messageString = arguments.getString(DIALOG_MESSAGE);

            if (dialogId == 0 || messageString == null) {
                throw new IllegalArgumentException("DIALOG_ID and/or DIALOG_MESSAGE not present in the bundle");
            }

            positiveStringId = arguments.getInt(DIALOG_POSITIVE_RID);
            if (positiveStringId == 0) {
                positiveStringId = R.string.ok;
            }

            negativeStringId = arguments.getInt(DIALOG_NEGATIVE_RID);
            if (negativeStringId == 0) {
                negativeStringId = R.string.cancel;
            }
        } else {
            throw new IllegalArgumentException("Must pass DIALOG_ID and DIALOG_MESSAGE in the bundle");
        }

        // we don't worry about calling dismiss method on the dialog fragment on click(), it handles automatically
        // but we should care when creating custom dialog and setting our custom view to it
        builder.setMessage(messageString)
                .setPositiveButton(positiveStringId, new DialogInterface.OnClickListener() {

                    /**
                     *
                     * @param dialogInterface Interface that defines a dialog-type class that can be shown,
                     *                        dismissed, or canceled, and may have buttons that can be clicked.
                     *
                     * @param which argument contains the index position of the selected item
                     */
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        // callback positive result method
                        if (mDialogEvents != null)
                            mDialogEvents.onPositiveDialogResult(dialogId, arguments);
                    }
                })
                .setNegativeButton(negativeStringId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        // callback negative result method
                        if (mDialogEvents != null)
                            mDialogEvents.onNegativeDialogResult(dialogId, arguments);
                    }
                });


        return builder.create();
    }

    /**
     * is called when out of dialog box is clicked or back button pressed or when calling cancel()
     * we can remove super method, it has no any logic
     * <p>
     * if onCancel is called, onDismiss also will be called, but opposite isn't true
     *
     * @param dialog
     */
    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        Log.d(TAG, "onCancel: called");
        if (mDialogEvents != null) {
            int dialogId = getArguments().getInt(DIALOG_ID);
            mDialogEvents.onDialogCancelled(dialogId);
        }
    }

    /**
     * called when calling dismiss(), or system automatically dismissing the dialog,
     * or even if it was dismissed as a result of being cancelled
     * we can't remove super method, if we do, there is some strange thing would happen,
     * dialog reappears when screen rotation
     * @param dialog
     */
//    @Override
//    public void onDismiss(@NonNull DialogInterface dialog) {
//        Log.d(TAG, "onDismiss: called");
//        super.onDismiss(dialog);
//    }
}
