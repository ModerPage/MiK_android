package me.modernpage.databinding;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.databinding.BindingAdapter;
import androidx.databinding.adapters.ListenerUtil;

import me.modernpage.activity.R;

public class ChangePasswordAdapter {

    @BindingAdapter(value = {"currentOnTextChanged", "confirmOnTextChanged", "newAfterTextChanged"}, requireAll = false)
    public static void setPasswordTextWatcher(EditText view, final CurrentOnTextChanged currentOnTextChanged,
                                              final ConfirmOnTextChanged confirmOnTextChanged,
                                              final NewAfterTextChanged newAfterTextChanged) {
        final TextWatcher newValue;
        if (currentOnTextChanged == null && confirmOnTextChanged == null && newAfterTextChanged == null) {
            newValue = null;
        } else {
            newValue = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (currentOnTextChanged != null) {
                        currentOnTextChanged.currentOnTextChanged(s, start, before, count);
                    }
                    if (confirmOnTextChanged != null) {
                        confirmOnTextChanged.confirmOnTextChanged(s, start, before, count);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (newAfterTextChanged != null) {
                        newAfterTextChanged.newAfterTextChanged(s);
                    }
                }
            };
        }
        final TextWatcher oldValue = ListenerUtil.trackListener(view, newValue, R.id.textWatcher);
        if (oldValue != null) {
            view.removeTextChangedListener(oldValue);
        }
        if (newValue != null) {
            view.addTextChangedListener(newValue);
        }
    }

    public interface CurrentOnTextChanged {
        void currentOnTextChanged(CharSequence s, int start, int before, int count);
    }

    public interface ConfirmOnTextChanged {
        void confirmOnTextChanged(CharSequence s, int start, int before, int count);
    }

    public interface NewAfterTextChanged {
        void newAfterTextChanged(Editable s);
    }
}
