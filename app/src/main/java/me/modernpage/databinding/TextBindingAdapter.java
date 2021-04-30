package me.modernpage.databinding;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.adapters.ListenerUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.modernpage.activity.R;
import me.modernpage.util.Constants;


public class TextBindingAdapter {
    private static final String TAG = "TextBindingAdapter";
    private static SimpleDateFormat mSimpleDateFormat =
            new SimpleDateFormat(Constants.DATE_FORMAT_PATTERN, Locale.getDefault());


    @BindingAdapter("birthdateText")
    public static void setBirthdate(EditText view, Date date) {
        if (date != null) {
            view.setText(mSimpleDateFormat.format(date));
        }
    }


    @BindingAdapter(value = {"emailText"})
    public static void setEmailText(EditText editText, String email) {
        Log.d(TAG, "setEmailText: called: " + email);
        if (email != null && !email.equals(editText.getText().toString())) {
            editText.setText(email);
        }
    }

    @InverseBindingAdapter(attribute = "emailText", event = "android:emailTextAttrChanged")
    public static String getEmailText(EditText editText) {
        Log.d(TAG, "getEmailText: " + editText.getText());
        if ((editText.getText() == null || "".equals(editText.getText().toString().trim()))) {
            editText.setError("This field can't be blank");
            return null;
        } else if (isNotValid(editText.getText().toString(), Constants.Regex.EMAIL)) {
            editText.setError("invalid email address");
            return null;
        }
        return String.valueOf(editText.getText());
    }


    @BindingAdapter(value = {"android:beforeTextChanged", "android:onTextChanged",
            "android:afterTextChanged", "android:emailTextAttrChanged"}, requireAll = false)
    public static void setEmailTextWatcher(EditText view, final BeforeTextChanged before,
                                           final OnTextChanged on, final AfterTextChanged after,
                                           final InverseBindingListener emailTextAttrChanged) {
        final TextWatcher newValue;
        if (before == null && after == null && on == null && emailTextAttrChanged == null) {
            newValue = null;
        } else {
            Log.d(TAG, "setEmailTextWatcher: called");
            newValue = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (before != null) {
                        before.beforeTextChanged(s, start, count, after);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (on != null) {
                        on.onTextChanged(s, start, before, count);
                    }
                    if (emailTextAttrChanged != null)
                        emailTextAttrChanged.onChange();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (after != null) {
                        after.afterTextChanged(s);
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


    @BindingAdapter(value = {"usernameText"})
    public static void setUsernameText(EditText editText, String username) {
        if (username != null && !username.equals(editText.getText().toString())) {
            editText.setText(username);
            Log.d(TAG, "setText: username " + username);
        }
    }

    @InverseBindingAdapter(attribute = "usernameText", event = "android:usernameTextAttrChanged")
    public static String getUsernameText(EditText editText) {
        Log.d(TAG, "getUsernameText: called");
        if ((editText.getText() == null || "".equals(editText.getText().toString().trim()))) {
            editText.setError("This field can't be blank");
            return null;
        } else if (isNotValid(editText.getText().toString(), Constants.Regex.USERNAME)) {
            editText.setError("Username must be 6~15 characters long and can contain \".\" \"_\" chars");
            return null;
        } else
            return String.valueOf(editText.getText());
    }

    @BindingAdapter(value = {"android:beforeTextChanged", "android:onTextChanged",
            "android:afterTextChanged", "android:usernameTextAttrChanged"}, requireAll = false)
    public static void setUsernameTextWatcher(EditText view, final BeforeTextChanged before,
                                              final OnTextChanged on, final AfterTextChanged after,
                                              final InverseBindingListener usernameTextAttrChanged) {
        Log.d(TAG, "setTextWatcher: called");
        final TextWatcher newValue;
        if (before == null && after == null && on == null && usernameTextAttrChanged == null) {
            newValue = null;
        } else {
            newValue = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (before != null) {
                        before.beforeTextChanged(s, start, count, after);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (on != null) {
                        on.onTextChanged(s, start, before, count);
                    }
                    if (usernameTextAttrChanged != null) {
                        usernameTextAttrChanged.onChange();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (after != null) {
                        after.afterTextChanged(s);
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

    @BindingAdapter(value = {"passwordText"})
    public static void setPasswordText(EditText editText, String password) {
        if (password != null && !password.equals(editText.getText().toString())) {
            editText.setText(password);
        }
    }

    @InverseBindingAdapter(attribute = "passwordText", event = "android:passwordTextAttrChanged")
    public static String getPasswordText(EditText editText) {
        if ((editText.getText() == null || "".equals(editText.getText().toString().trim()))) {
            editText.setError("This field can't be blank");
            return null;
        } else if (isNotValid(editText.getText().toString(), Constants.Regex.PASSWORD)) {
            editText.setError("Password must be 8~20 characters long and contain a digit.");
            return null;
        } else
            return String.valueOf(editText.getText());
    }

    @BindingAdapter(value = {"android:beforeTextChanged", "android:onTextChanged",
            "android:afterTextChanged", "android:passwordTextAttrChanged"}, requireAll = false)
    public static void setPasswordTextWatcher(EditText view, final BeforeTextChanged before,
                                              final OnTextChanged on, final AfterTextChanged after,
                                              final InverseBindingListener passwordTextAttrChanged) {
        final TextWatcher newValue;
        if (before == null && after == null && on == null && passwordTextAttrChanged == null) {
            newValue = null;
        } else {
            newValue = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (before != null) {
                        before.beforeTextChanged(s, start, count, after);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (on != null) {
                        on.onTextChanged(s, start, before, count);
                    }
                    if (passwordTextAttrChanged != null)
                        passwordTextAttrChanged.onChange();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (after != null) {
                        after.afterTextChanged(s);
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

    @BindingAdapter(value = {"fullnameText"})
    public static void setFullnameText(EditText editText, String fullname) {
        if (fullname != null && !fullname.equals(editText.getText().toString())) {
            editText.setText(fullname);
            Log.d(TAG, "setText: fullname: " + fullname);
        }
    }

    @InverseBindingAdapter(attribute = "fullnameText", event = "android:fullnameTextAttrChanged")
    public static String getFullnameText(EditText editText) {
        Log.d(TAG, "getPasswordText: called");
        if ((editText.getText() == null || "".equals(editText.getText().toString().trim()))) {
            editText.setError("This field can't be blank");
            return null;
        } else if (isNotValid(editText.getText().toString(), "^[A-Za-z].{8,20}$")) {
            editText.setError("Fullname must be 8~20 characters long.");
            return null;
        } else
            return String.valueOf(editText.getText());
    }

    @BindingAdapter(value = {"android:beforeTextChanged", "android:onTextChanged",
            "android:afterTextChanged", "android:fullnameTextAttrChanged"}, requireAll = false)
    public static void setFullnameTextWatcher(EditText view, final BeforeTextChanged before,
                                              final OnTextChanged on, final AfterTextChanged after,
                                              final InverseBindingListener fullnameTextAttrChanged) {
        final TextWatcher newValue;
        if (before == null && after == null && on == null && fullnameTextAttrChanged == null) {
            newValue = null;
        } else {
            newValue = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if (before != null) {
                        before.beforeTextChanged(s, start, count, after);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (on != null) {
                        on.onTextChanged(s, start, before, count);
                    }
                    if (fullnameTextAttrChanged != null)
                        fullnameTextAttrChanged.onChange();
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (after != null) {
                        after.afterTextChanged(s);
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


    public static boolean isNotValid(String field, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(field);
        return !matcher.matches();
    }

    public interface AfterTextChanged {
        void afterTextChanged(Editable s);
    }

    public interface BeforeTextChanged {
        void beforeTextChanged(CharSequence s, int start, int count, int after);
    }

    public interface OnTextChanged {
        void onTextChanged(CharSequence s, int start, int before, int count);
    }

}
