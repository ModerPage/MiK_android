package me.modernpage.ui.register;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.base.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivityRegisterBinding;
import me.modernpage.databinding.ChangePasswordAdapter;
import me.modernpage.ui.BaseActivity;
import me.modernpage.util.Constants;

@AndroidEntryPoint
public class RegisterActivity extends BaseActivity<ActivityRegisterBinding>
        implements ChangePasswordAdapter.NewAfterTextChanged, ChangePasswordAdapter.ConfirmOnTextChanged {
    private static final String TAG = "RegisterActivity";
    RegisterViewModel mRegisterViewModel;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_register;
    }


    public interface Handler {
        void onRegister();

        void onLogin();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRegisterViewModel = new ViewModelProvider(this).get(RegisterViewModel.class);
        dataBinding.setViewModel(mRegisterViewModel);
        dataBinding.setHandler(mHandler);
        dataBinding.setConfirmOnTextChanged(this);
        dataBinding.setNewAfterTextChanged(this);
        dataBinding.setUsernameTextChanged((s, start, before, count) -> {
            dataBinding.registerUsername.setBackground(
                    ContextCompat.getDrawable(this, R.drawable.textfield_bg));
            if (Strings.isNullOrEmpty(s.toString())) {
                dataBinding.registerUsername
                        .setError("This field can't be blank");
            }
            if (isNotValid(s.toString(), Constants.Regex.USERNAME)) {
                dataBinding.registerUsername
                        .setError("Username must be 6~15 characters long and can contain \".\" \"_\" chars");
                return;
            }
            mRegisterViewModel.checkUsername(s.toString());
        });

        dataBinding.setEmailTextChanged((s, start, before, count) -> {
            dataBinding.registerEmail.setBackground(
                    ContextCompat.getDrawable(this, R.drawable.textfield_bg));

            if (Strings.isNullOrEmpty(s.toString())) {
                dataBinding.registerEmail
                        .setError("This field can't be blank");
            }

            if (isNotValid(s.toString(), Constants.Regex.EMAIL)) {
                dataBinding.registerEmail.setError("Invalid email address");
                return;
            }
            mRegisterViewModel.checkEmail(s.toString());
        });

        mRegisterViewModel.getRegisterState().observe(this, state -> {
            if (state == null) {
                dataBinding.setProcessState(false);
            } else {
                dataBinding.setProcessState(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                }
                Boolean data = state.getData();
                if (data != null && data) {
                    Intent intent = new Intent(RegisterActivity.this, RegistrationSuccessActivity.class);
                    startActivity(intent);
                }
            }
            dataBinding.executePendingBindings();
        });

        mRegisterViewModel.getUsernameValidateState().observe(this, state -> {
            if (state == null) {
                dataBinding.registerUsername.setBackground(ContextCompat.getDrawable(this, R.drawable.textfield_bg));
            } else {
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    dataBinding.registerUsername.setBackground(ContextCompat.getDrawable(this, R.drawable.textfield_error_bg));
                    dataBinding.registerUsername.setError(error);
                }
                Boolean data = state.getData();
                if (data != null && data) {
                    dataBinding.registerUsername.setBackground(
                            ContextCompat.getDrawable(this, R.drawable.textfield_correct_bg));
                    mRegisterViewModel.setUsername(dataBinding.registerUsername.getText().toString());
                }
            }
            dataBinding.executePendingBindings();
        });

        mRegisterViewModel.getEmailValidateState().observe(this, state -> {
            if (state == null) {
                dataBinding.registerEmail.setBackground(ContextCompat.getDrawable(this, R.drawable.textfield_bg));
            } else {
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    dataBinding.registerEmail.setBackground(ContextCompat.getDrawable(this, R.drawable.textfield_error_bg));
                    dataBinding.registerEmail.setError(error);
                }
                Boolean data = state.getData();
                if (data != null && data) {
                    dataBinding.registerEmail.setBackground(
                            ContextCompat.getDrawable(this, R.drawable.textfield_correct_bg));
                    mRegisterViewModel.setEmail(dataBinding.registerEmail.getText().toString());
                }
            }
            dataBinding.executePendingBindings();
        });
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void onRegister() {
            hideKeyboard();
            mRegisterViewModel.register();
        }

        @Override
        public void onLogin() {
            finish();
        }
    };


    @Override
    public void confirmOnTextChanged(CharSequence s, int start, int before, int count) {
        String newPassword = dataBinding.registerPassword.getText().toString();
        System.out.println(s + ", " + newPassword);
        if (newPassword != null && !newPassword.contentEquals(s)) {
            dataBinding.registerConfirmpassword
                    .setError("Passwords do not match.");
        }
        mRegisterViewModel.setConfirmPassword(s.toString());
    }

    @Override
    public void newAfterTextChanged(Editable s) {
        if (s != null && s.toString().trim().length() > 0) {
            if (isNotValid(s.toString(), Constants.Regex.PASSWORD)) {
                dataBinding.registerPassword
                        .setError("Password must be 8~20 characters long and contain a digit.");
            }

            String confirmPassword = dataBinding.registerConfirmpassword.getText().toString();
            if (confirmPassword != null && confirmPassword.equals(s.toString())) {
                dataBinding.registerConfirmpassword.setError(null);
            } else {
                dataBinding.registerConfirmpassword.setError("Passwords do not match.");
            }
            mRegisterViewModel.setPassword(s.toString());
        }
    }

    private boolean isNotValid(String field, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(field);
        return !matcher.matches();
    }
}
