package me.modernpage.ui.login;

import android.view.View;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.common.base.Strings;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.remote.model.LoginRequest;
import me.modernpage.data.remote.model.PasswordRequest;
import me.modernpage.data.repository.UserRepository;
import me.modernpage.ui.common.ProcessHandler;
import me.modernpage.util.LoadState;

@HiltViewModel
public class LoginViewModel extends ViewModel {
    private static final String TAG = "LoginViewModel";
    private final MutableLiveData<Boolean> rememberMe;
    private final SavedStateHandle mSavedStateHandle;

    private final MutableLiveData<String> username;
    private final MutableLiveData<String> password;
    private MutableLiveData<String> email;

    private MutableLiveData<String> newPassword;
    private MutableLiveData<String> confirmPassword;

    private final ProcessLoginHandler mLoginHandler;
    private final EmailVerifyHandler mEmailVerifyHandler;
    private final ResetPasswordHandler mResetPasswordHandler;

    @Inject
    public LoginViewModel(UserRepository userRepository, SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mLoginHandler = new ProcessLoginHandler(userRepository);
        mEmailVerifyHandler = new EmailVerifyHandler(userRepository);
        mResetPasswordHandler = new ResetPasswordHandler(userRepository);

        username = savedStateHandle.getLiveData(LoginActivity.LOGIN_USERNAME);
        password = savedStateHandle.getLiveData(LoginActivity.LOGIN_PASSWORD);
        rememberMe = savedStateHandle.getLiveData(LoginActivity.LOGIN_REMEMBER_ME);
    }

    public MutableLiveData<String> getUsername() {
        return username;
    }

    public MutableLiveData<String> getPassword() {
        return password;
    }

    public void setPassword(String password) {
        mSavedStateHandle.set(LoginActivity.LOGIN_PASSWORD, password);
    }

    public void setUsername(String username) {
        mSavedStateHandle.set(LoginActivity.LOGIN_USERNAME, username);
    }

    public void setRememberMe(boolean rememberMe) {
        mSavedStateHandle.set(LoginActivity.LOGIN_REMEMBER_ME, rememberMe);
    }

    public MutableLiveData<Boolean> getRememberMe() {
        return rememberMe;
    }

    public void setEmail(String email) {
        this.email.setValue(email);
    }

    public MutableLiveData<String> getEmail() {
        if (email == null)
            email = new MutableLiveData<>();
        return email;
    }

    public MutableLiveData<String> getNewPassword() {
        if (newPassword == null)
            newPassword = new MutableLiveData<>();
        return newPassword;
    }

    public MutableLiveData<String> getConfirmPassword() {
        if (confirmPassword == null)
            confirmPassword = new MutableLiveData<>();
        return confirmPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword.setValue(newPassword);
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword.setValue(confirmPassword);
    }

    public void checkAuth(String username) {
        mLoginHandler.checkAuth(username);
    }

    public void loginClick() {
        String username = getUsername().getValue();
        String password = getPassword().getValue();
        if (username != null && password != null) {
            mLoginHandler.login(new LoginRequest(username, password));
        }
    }

    public void checkEmail(View view) {
        final String email = getEmail().getValue();
        if (email != null && email.trim().length() > 0) {
            mEmailVerifyHandler.checkEmail(email);
        }
    }

    public void resetPasswordClick(View view) {
        final String newPasswordValue = newPassword.getValue();
        final String confirmPasswordValue = confirmPassword.getValue();
        final String emailValue = email.getValue();
        if (Strings.isNullOrEmpty(emailValue) || Strings.isNullOrEmpty(newPasswordValue) ||
                Strings.isNullOrEmpty(confirmPasswordValue))
            return;
        if (!newPasswordValue.equals(confirmPasswordValue))
            return;

        mResetPasswordHandler.resetPassword(new PasswordRequest(emailValue, newPasswordValue));
    }


    public LiveData<LoadState<Boolean>> getEmailVerifyState() {
        return mEmailVerifyHandler.getProcessState();
    }

    public LiveData<LoadState<Boolean>> getResetPasswordState() {
        return mResetPasswordHandler.getProcessState();
    }

    public void resetForgetPasswordProcess() {
        email.setValue(null);
        newPassword.setValue(null);
        confirmPassword.setValue(null);
        mEmailVerifyHandler.reset();
        mResetPasswordHandler.reset();
    }

    public LiveData<LoadState<Profile>> getLoginLoadState() {
        return mLoginHandler.getProcessState();
    }

    static class ProcessLoginHandler extends ProcessHandler<Profile> {
        private final UserRepository repository;

        ProcessLoginHandler(UserRepository repository) {
            this.repository = repository;
        }

        public void login(LoginRequest loginRequest) {
            unregister();
            data = repository.loginProcess(loginRequest);
            processState.setValue(new LoadState<Profile>(true, true, null, null));
            data.observeForever(this);
        }

        public void checkAuth(String username) {
            unregister();
            data = repository.checkAuth(username);
            processState.setValue(new LoadState<Profile>(true, true, null, null));
            data.observeForever(this);
        }

    }


    static class EmailVerifyHandler extends ProcessHandler<Boolean> {
        private final UserRepository repository;

        EmailVerifyHandler(UserRepository repository) {
            this.repository = repository;
        }

        public void checkEmail(String email) {
            unregister();
            data = repository.checkEmail(email);
            processState.setValue(new LoadState<Boolean>(true, true, null, null));
            data.observeForever(this);
        }

        @Override
        protected void reset() {
            super.reset();
        }
    }


    static class ResetPasswordHandler extends ProcessHandler<Boolean> {
        private final UserRepository repository;

        ResetPasswordHandler(UserRepository repository) {
            this.repository = repository;
        }

        public void resetPassword(PasswordRequest passwordRequest) {
            unregister();
            data = repository.resetPassword(passwordRequest);
            processState.setValue(new LoadState<Boolean>(true, true, null, null));
            data.observeForever(this);
        }

        @Override
        protected void reset() {
            super.reset();
        }
    }
}
