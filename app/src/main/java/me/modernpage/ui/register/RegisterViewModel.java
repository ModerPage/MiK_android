package me.modernpage.ui.register;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.common.base.Strings;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import me.modernpage.data.remote.model.RegisterRequest;
import me.modernpage.data.repository.UserRepository;
import me.modernpage.ui.common.ProcessHandler;
import me.modernpage.util.LoadState;

@HiltViewModel
public class RegisterViewModel extends ViewModel {
    private static final String TAG = "UserViewModel";
    private final MutableLiveData<String> username;
    private final MutableLiveData<String> fullname;
    private final MutableLiveData<String> password;
    private final MutableLiveData<String> confirmPassword;
    private final MutableLiveData<String> email;
    private final RegisterHandler mRegisterHandler;
    private final SavedStateHandle mSavedStateHandle;

    @Inject
    public RegisterViewModel(UserRepository userRepository, SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mRegisterHandler = new RegisterHandler(userRepository);
        username = mSavedStateHandle.getLiveData("username");
        fullname = mSavedStateHandle.getLiveData("fullname");
        email = mSavedStateHandle.getLiveData("email");
        password = mSavedStateHandle.getLiveData("password");
        confirmPassword = mSavedStateHandle.getLiveData("confirmPassword");

    }

    public void setUsername(String username) {
        mSavedStateHandle.set("username", username);
    }

    public void setFullname(String fullname) {
        mSavedStateHandle.set("fullname", fullname);
    }

    public void setPassword(String password) {
        mSavedStateHandle.set("password", password);
    }

    public void setConfirmPassword(String confirmPassword) {
        mSavedStateHandle.set("confirmPassword", confirmPassword);
    }

    public void setEmail(String email) {
        mSavedStateHandle.set("email", email);
    }

    public MutableLiveData<String> getUsername() {
        return username;
    }

    public MutableLiveData<String> getFullname() {
        return fullname;
    }

    public MutableLiveData<String> getPassword() {
        return password;
    }

    public MutableLiveData<String> getConfirmPassword() {
        return confirmPassword;
    }

    public MutableLiveData<String> getEmail() {
        return email;
    }

    public void register() {
        String username = this.username.getValue();
        String fullname = this.fullname.getValue();
        String email = this.email.getValue();
        String password = this.password.getValue();
        String confirmPassword = this.confirmPassword.getValue();
        if (Strings.isNullOrEmpty(username) || Strings.isNullOrEmpty(fullname) ||
                Strings.isNullOrEmpty(email) || Strings.isNullOrEmpty(password) ||
                Strings.isNullOrEmpty(confirmPassword)) {
            return;
        }

        if (!password.equals(confirmPassword))
            return;

        mRegisterHandler.register(new RegisterRequest(username, fullname, email, password));
    }

    public LiveData<LoadState<Boolean>> getRegisterState() {
        return mRegisterHandler.getProcessState();
    }

    static class RegisterHandler extends ProcessHandler<Boolean> {
        private final UserRepository repository;

        RegisterHandler(UserRepository repository) {
            this.repository = repository;
        }

        public void register(RegisterRequest registerRequest) {
            unregister();
            data = repository.register(registerRequest);
            processState.setValue(new LoadState<Boolean>(true, true, null, null));
            data.observeForever(this);
        }
    }
}
