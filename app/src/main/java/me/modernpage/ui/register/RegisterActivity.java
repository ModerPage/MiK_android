package me.modernpage.ui.register;

import android.os.Bundle;
import android.text.Editable;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

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
                    finish();
                }
            }

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
            if (isNotValid(s.toString())) {
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

    private boolean isNotValid(String field) {
        Pattern pattern = Pattern.compile(Constants.Regex.PASSWORD);
        Matcher matcher = pattern.matcher(field);
        return !matcher.matches();
    }
}
