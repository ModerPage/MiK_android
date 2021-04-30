package me.modernpage.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivityLoginBinding;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.ui.BaseActivity;
import me.modernpage.ui.BaseDialog;
import me.modernpage.ui.dialog.InputEmailDialog;
import me.modernpage.ui.dialog.ResetPasswordDialog;
import me.modernpage.ui.main.MainActivity;
import me.modernpage.ui.register.RegisterActivity;

@AndroidEntryPoint
public class LoginActivity extends BaseActivity<ActivityLoginBinding> implements BaseDialog.DialogEvents {
    private static final String TAG = "LoginActivity";
    static final String LOGIN_USERNAME = "login_username";
    static final String LOGIN_PASSWORD = "login_password";
    static final String LOGIN_REMEMBER_ME = "login_remember_me";
    private static final int DIALOG_ID_MAIL_INPUT = 1;
    private static final int DIALOG_ID_RESET_PASSWORD = 2;

    LoginViewModel mLoginViewModel;

    @Override
    public int getLayoutRes() {
        return R.layout.activity_login;
    }

    public interface Handler {
        void onForgetPassword();

        void onRegister();

        void onLogin();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoginViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        dataBinding.setLoginViewModel(mLoginViewModel);
        dataBinding.setHandler(mHandler);

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        final String savedUsername = sharedPreferences.getString(LOGIN_USERNAME, null);
        boolean rememberMe = sharedPreferences.getBoolean(LOGIN_REMEMBER_ME, false);
        mLoginViewModel.setRememberMe(rememberMe);
        if (rememberMe) {
            String password = sharedPreferences.getString(LOGIN_PASSWORD, null);
            mLoginViewModel.setUsername(savedUsername);
            mLoginViewModel.setPassword(password);
        }

        mLoginViewModel.checkAuth(savedUsername);
        mLoginViewModel.getLoginLoadState().observe(this, state -> {
            if (state == null) {
                dataBinding.setProcessState(false);
            } else {
                dataBinding.setProcessState(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                }
                if (error == null && state.getData() != null) {
                    Boolean remember = mLoginViewModel.getRememberMe().getValue();
                    String username = mLoginViewModel.getUsername().getValue();
                    String password = mLoginViewModel.getPassword().getValue();
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    if (remember != null && remember) {
                        editor.putString(LOGIN_PASSWORD, password)
                                .putBoolean(LOGIN_REMEMBER_ME, true);
                    } else {
                        editor.clear().apply();
                    }
                    editor.putString(LOGIN_USERNAME, username != null ? username : savedUsername).apply();
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra(Profile.class.getSimpleName(), state.getData().getId());
                    startActivity(intent);
                    finish();
                }
            }
            dataBinding.executePendingBindings();
        });

    }

    private final Handler mHandler = new Handler() {
        @Override
        public void onForgetPassword() {
            hideKeyboard();
            InputEmailDialog email_input_dialog = new InputEmailDialog();
            Bundle args = new Bundle();
            args.putInt(BaseDialog.DIALOG_ID, DIALOG_ID_MAIL_INPUT);
            email_input_dialog.setArguments(args);
            email_input_dialog.show(getSupportFragmentManager(), null);
        }

        @Override
        public void onRegister() {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }

        @Override
        public void onLogin() {
            hideKeyboard();
            mLoginViewModel.loginClick();
        }
    };

    @Override
    public void onPositiveDialogResult(int dialogId) {
        switch (dialogId) {
            case DIALOG_ID_MAIL_INPUT:
                ResetPasswordDialog resetPasswordDialog = new ResetPasswordDialog();
                Bundle args = new Bundle();
                args.putInt(BaseDialog.DIALOG_ID, DIALOG_ID_RESET_PASSWORD);
                resetPasswordDialog.setArguments(args);
                resetPasswordDialog.show(getSupportFragmentManager(), null);
                break;
            case DIALOG_ID_RESET_PASSWORD:
                hideKeyboard();
                Toast.makeText(LoginActivity.this,
                        "Password changed successfully", Toast.LENGTH_LONG).show();
                mLoginViewModel.resetForgetPasswordProcess();
                break;
        }
    }

    @Override
    public void onNegativeDialogResult(int dialogId) {

    }
}
