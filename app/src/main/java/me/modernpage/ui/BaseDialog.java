package me.modernpage.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import javax.inject.Inject;

import me.modernpage.databinding.CustomDataBindingComponent;

public abstract class BaseDialog<DB extends ViewDataBinding, VM extends ViewModel> extends AppCompatDialogFragment {
    public DB dataBinding;
    public VM viewModel;
    private static final String TAG = "BaseDialog";

    public static final String DIALOG_ID = "id";
    @Inject
    protected CustomDataBindingComponent dataBindingComponent;

    public interface DialogEvents {
        void onPositiveDialogResult(int dialogId);

        void onNegativeDialogResult(int dialogId);
    }

    protected DialogEvents mDialogEvent;

    @LayoutRes
    public abstract int getLayoutRes();

    public abstract Class<VM> getViewModel();

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach: called");
        super.onAttach(context);
        if (!(context instanceof BaseDialog.DialogEvents)) {
            throw new ClassCastException(context.toString() + "must implement BaseDialog.DialogEvents interface");
        }
        mDialogEvent = (DialogEvents) context;
        viewModel = new ViewModelProvider(getActivity()).get(getViewModel());
        dataBinding = DataBindingUtil.inflate(LayoutInflater.from(context), getLayoutRes(), null, false, dataBindingComponent);
        dataBinding.setLifecycleOwner(this);
    }


    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: called");
        super.onDetach();
        mDialogEvent = null;
    }
}
