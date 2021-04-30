package me.modernpage.ui.settings;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.google.common.base.Strings;

import java.io.File;
import java.util.Date;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import me.modernpage.data.remote.model.AccountInfo;
import me.modernpage.data.remote.model.PasswordRequest;
import me.modernpage.data.repository.UserRepository;
import me.modernpage.ui.common.ProcessHandler;
import me.modernpage.util.App;
import me.modernpage.util.FileUtils;
import me.modernpage.util.LoadState;

@HiltViewModel
public class SettingsViewModel extends ViewModel {
    private static final String TAG = "SettingsViewModel";
    private final MutableLiveData<String> fullname;
    private final MutableLiveData<String> username;
    private final MutableLiveData<String> email;
    private final MutableLiveData<String> password;
    private final MutableLiveData<String> imageURL;
    private final MutableLiveData<Date> birthdate;
    private final LiveData<Uri> uploadedImage;

    private MutableLiveData<String> currentPassword;
    private MutableLiveData<String> newPassword;
    private MutableLiveData<String> confirmPassword;

    private final SavedStateHandle mSavedStateHandle;
    private final ProcessUserHandler mProcessUserHandler;
    private final AccountInfoHandler mAccountInfoHandler;
    private final PasswordCheckHandler mPasswordCheckHandler;
    private final UserRepository mUserRepository;

    @Inject
    public SettingsViewModel(UserRepository userRepository, SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        mProcessUserHandler = new ProcessUserHandler(userRepository);
        mAccountInfoHandler = new AccountInfoHandler(userRepository);
        mPasswordCheckHandler = new PasswordCheckHandler(userRepository);
        mUserRepository = userRepository;

        fullname = mSavedStateHandle.getLiveData("fullname");
        username = mSavedStateHandle.getLiveData("username");
        email = mSavedStateHandle.getLiveData("email");
        password = mSavedStateHandle.getLiveData("password");
        imageURL = mSavedStateHandle.getLiveData("imageURL");
        birthdate = mSavedStateHandle.getLiveData("birthdate");
        uploadedImage = mSavedStateHandle.getLiveData("uploadedImage");
    }

    public void getAccountInfo(long profileId) {
        mAccountInfoHandler.getAccountInfo(profileId);
    }

    public void setFullname(String fullname) {
        mSavedStateHandle.set("fullname", fullname);
    }

    public void setUsername(String username) {
        mSavedStateHandle.set("username", username);
    }

    public void setEmail(String email) {
        mSavedStateHandle.set("email", email);
    }

    public void setPassword(String password) {
        mSavedStateHandle.set("password", password);
    }

    public void setImageURL(String imageURL) {
        mSavedStateHandle.set("imageURL", imageURL);
    }

    public void setBirthdate(Date birthdate) {
        this.birthdate.setValue(birthdate);
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword.setValue(currentPassword);
    }

    public void setNewPassword(String newPassword) {
        this.newPassword.setValue(newPassword);
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword.setValue(confirmPassword);
    }

    public MutableLiveData<String> getFullname() {
        return fullname;
    }

    public MutableLiveData<String> getUsername() {
        return username;
    }

    public MutableLiveData<String> getEmail() {
        return email;
    }

    public MutableLiveData<String> getPassword() {
        return password;
    }

    public MutableLiveData<String> getImageURL() {
        return imageURL;
    }

    public MutableLiveData<Date> getBirthdate() {
        return birthdate;
    }

    public LiveData<Uri> getUploadedImage() {
        return uploadedImage;
    }

    public void checkPassword(String password) {
        AccountInfo accountInfo = getAccountInfoState().getValue().getData();
        if (accountInfo == null)
            return;
        mPasswordCheckHandler.check(new PasswordRequest(accountInfo.getEmail(), password));
    }

    public MutableLiveData<String> getCurrentPassword() {
        if (currentPassword == null)
            currentPassword = new MutableLiveData<>();
        return currentPassword;
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

    public void setUploadedImage(Uri uploadedImage) {
        mSavedStateHandle.set("uploadedImage", uploadedImage);
    }

    public LiveData<LoadState<Boolean>> getProcessState() {
        return mProcessUserHandler.getProcessState();
    }

    public LiveData<LoadState<AccountInfo>> getAccountInfoState() {
        return mAccountInfoHandler.getProcessState();
    }

    public void passwordDialogReset() {
        mPasswordCheckHandler.reset();
        currentPassword = null;
        newPassword = null;
        confirmPassword = null;
    }

    public boolean confirmChangePassword() {
        String currentPasswordValue = currentPassword.getValue();
        String newPasswordValue = newPassword.getValue();
        String confirmPasswordValue = confirmPassword.getValue();

        if (Strings.isNullOrEmpty(currentPasswordValue) ||
                Strings.isNullOrEmpty(newPasswordValue) ||
                Strings.isNullOrEmpty(confirmPasswordValue)) {
            return false;
        }

        LoadState<Boolean> value = mPasswordCheckHandler.getProcessState().getValue();
        Boolean data = value.getData();
        if (data == null || !data)
            return false;

        return newPasswordValue.equals(confirmPasswordValue);
    }

    public LiveData<LoadState<Boolean>> getPasswordCheckState() {
        return mPasswordCheckHandler.getProcessState();
    }

    static class ProcessUserHandler extends ProcessHandler<Boolean> {
        private final UserRepository repository;

        public ProcessUserHandler(UserRepository repository) {
            this.repository = repository;
        }

        public void update(long profileId, AccountInfo accountInfo, File avatar) {
            unregister();
            data = repository.update(profileId, accountInfo, avatar);
            processState.setValue(new LoadState<Boolean>(true, true, null, null));
            data.observeForever(this);
        }
    }

    static class AccountInfoHandler extends ProcessHandler<AccountInfo> {
        private final UserRepository repository;

        public AccountInfoHandler(UserRepository repository) {
            this.repository = repository;
        }

        public void getAccountInfo(long profileId) {
            unregister();
            data = repository.getAccountInfoByProfileId(profileId);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }
    }

    static class PasswordCheckHandler extends ProcessHandler<Boolean> {
        private final UserRepository mUserRepository;

        public PasswordCheckHandler(UserRepository userRepository) {
            mUserRepository = userRepository;
        }

        public void check(PasswordRequest passwordRequest) {
            unregister();
            data = mUserRepository.checkPassword(passwordRequest);
            processState.setValue(new LoadState<>(true, true, null, null));
            data.observeForever(this);
        }

        @Override
        protected void reset() {
            super.reset();
        }
    }

    public void relogin() {
        mUserRepository.relogin();
    }

    public boolean canClose() {
        AccountInfo accountInfo = getAccountInfoState().getValue().getData();
        if (accountInfo == null)
            return true;

        String fullnameValue = fullname.getValue();
        String usernameValue = username.getValue();
        String passwordValue = password.getValue();
        Date birthdateValue = birthdate.getValue();
        Uri uploadedImageValue = uploadedImage.getValue();

        return accountInfo.getFullname().equals(fullnameValue) &&
                accountInfo.getUsername().equals(usernameValue) &&
                accountInfo.getBirthdate().equals(birthdateValue) &&
                uploadedImageValue == null &&
                Strings.isNullOrEmpty(passwordValue);
    }

    public void updateProfile(long profileId) {
        AccountInfo accountInfo = getAccountInfoState().getValue().getData();
        if (accountInfo == null)
            return;

        String fullnameValue = fullname.getValue();
        String usernameValue = username.getValue();
        String passwordValue = password.getValue();
        Date birthdateValue = birthdate.getValue();

        if (Strings.isNullOrEmpty(fullnameValue) ||
                Strings.isNullOrEmpty(usernameValue) ||
                birthdateValue == null) {
            return;
        }

        File avatar = null;
        Uri imageValue = uploadedImage.getValue();
        if (imageValue != null) {
            try {
                avatar = FileUtils.getFileFromUri(App.getInstance(), imageValue);
            } catch (Exception e) {
                Log.e(TAG, "updateProfileClicked: Exception" + e.getMessage(), e);
            }
        }
        AccountInfo newAccountInfo =
                new AccountInfo(usernameValue, accountInfo.getEmail(), fullnameValue, birthdateValue, passwordValue,
                        null);
        mProcessUserHandler.update(profileId, newAccountInfo, avatar);
    }
}
