package me.modernpage.ui.settings;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.Calendar;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivitySettingsBinding;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.remote.model.AccountInfo;
import me.modernpage.ui.BaseActivity;
import me.modernpage.ui.BaseDialog;
import me.modernpage.ui.dialog.AppDialog;
import me.modernpage.ui.dialog.ChangePasswordDialog;
import me.modernpage.ui.dialog.DatePickerDialog;
import me.modernpage.ui.login.LoginActivity;

@AndroidEntryPoint
public class SettingsActivity extends BaseActivity<ActivitySettingsBinding>
        implements BaseDialog.DialogEvents, AppDialog.DialogEvents {
    private static final int DIALOG_ID_CHANGE_PASSWORD = 100;
    private static final String TAG = "SettingsActivity";
    private static final int DIALOG_ID_CANCEL_EDIT = 1;

    SettingsViewModel mSettingsViewModel;
    private Uri mCapturedImageUri;
    private static boolean CAPTURE_PHOTO_PERMISSION_GRANTED = false;
    private static boolean PICK_IMAGE_PERMISSION_GRANTED = false;

    public interface Handler {
        void uploadImageClicked();

        void resetPasswordClicked();

        void setBirthdateClicked();

        void updateProfileClicked();
    }

    private final ActivityResultLauncher<Uri> mCropImageForResult = registerForActivityResult(
            new CropImageContract(), result -> {
                if (result != null) {
                    mSettingsViewModel.setUploadedImage(result);
                    ParcelFileDescriptor parcelFD = null;
                    try {
                        parcelFD = getContentResolver().openFileDescriptor(result, "r");
                        FileDescriptor imageSource = parcelFD.getFileDescriptor();
                        // Decode image size
                        BitmapFactory.Options o = new BitmapFactory.Options();
                        o.inJustDecodeBounds = true;
                        BitmapFactory.decodeFileDescriptor(imageSource, null, o);

                        // the new size we want to scale to
                        final int REQUIRED_SIZE = 1024;

                        // Find the correct scale value. It should be the power of 2.
                        int width_tmp = o.outWidth, height_tmp = o.outHeight;
                        int scale = 1;

                        while (width_tmp >= REQUIRED_SIZE || height_tmp >= REQUIRED_SIZE) {
                            width_tmp /= 2;
                            height_tmp /= 2;
                            scale *= 2;
                        }

                        // decode with inSampleSize
                        BitmapFactory.Options o2 = new BitmapFactory.Options();
                        o2.inSampleSize = scale;
                        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(imageSource, null, o2);
                        dataBinding.settingsUpdateAvatar.setImageBitmap(bitmap);
                    } catch (FileNotFoundException e) {
                        Log.e(TAG, "CaptureImageForResult: FileNotFoundException ", e);
                    }
                }
            });

    private final ActivityResultLauncher<Uri> mCaptureImageForResult = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), result -> {
                if (result && mCapturedImageUri != null) {
                    mCropImageForResult.launch(mCapturedImageUri);
                }
            });

    private final ActivityResultLauncher<String> mPickImageForResult = registerForActivityResult(
            new ActivityResultContracts.GetContent(), result -> {
                if (result != null) {
                    mCropImageForResult.launch(result);
                }
            });

    @Override
    public void onPositiveDialogResult(int dialogId, Bundle args) {

    }

    @Override
    public void onNegativeDialogResult(int dialogId, Bundle args) {
        finish();
    }

    @Override
    public void onDialogCancelled(int dialogId) {

    }

    static class CropImageContract extends ActivityResultContract<Uri, Uri> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Uri input) {
            return CropImage.activity(input)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.OVAL)
                    .setAspectRatio(1, 1)
                    .getIntent(context);
        }

        @Override
        public Uri parseResult(int resultCode, @Nullable Intent intent) {
            CropImage.ActivityResult result = CropImage.getActivityResult(intent);
            return result != null ? result.getUri() : null;
        }
    }

    private final ActivityResultLauncher<String[]> mUploadImageRequestPermissionForResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (result.containsKey(Manifest.permission.CAMERA) && result.containsKey(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    CAPTURE_PHOTO_PERMISSION_GRANTED = result.get(Manifest.permission.CAMERA) && result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (result.containsKey(Manifest.permission.READ_EXTERNAL_STORAGE))
                    PICK_IMAGE_PERMISSION_GRANTED = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
    );

    @Override
    public int getLayoutRes() {
        return R.layout.activity_settings;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        final long profileId;
        Bundle args = getIntent().getExtras();
        if (args != null) {
            profileId = args.getLong(Profile.class.getSimpleName());
            if (profileId == 0)
                throw new IllegalArgumentException("Profile id is not present in the bundle");
        } else {
            throw new IllegalArgumentException("Must pass profile id in bundle.");
        }
        mSettingsViewModel.getAccountInfo(profileId);
        dataBinding.setViewModel(mSettingsViewModel);
        dataBinding.setHandler(mHandler);


        mSettingsViewModel.getAccountInfoState().observe(this, state -> {
            if (state == null) {
                dataBinding.setProcessState(false);
            } else {
                dataBinding.setProcessState(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (state.isVerified())
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", view -> logout()).show();
                }
                AccountInfo data = state.getData();
                if (data != null) {
                    mSettingsViewModel.setUsername(data.getUsername());
                    mSettingsViewModel.setEmail(data.getEmail());
                    mSettingsViewModel.setFullname(data.getFullname());
                    mSettingsViewModel.setImageURL(data.getAvatar());
                    mSettingsViewModel.setBirthdate(data.getBirthdate());
                }
            }
            dataBinding.executePendingBindings();
        });

        Toolbar toolbar = (Toolbar) dataBinding.settingsToolbar;
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSettingsViewModel.getProcessState().observe(this, state -> {
            if (state == null) {
                dataBinding.setProcessState(false);
            } else {
                dataBinding.setProcessState(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (state.isVerified())
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", view -> logout()).show();
                }
                Boolean data = state.getData();
                if (data != null && data) {
                    finish();
                }
            }
            dataBinding.executePendingBindings();
        });
    }

    private void logout() {
        mSettingsViewModel.relogin();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        CAPTURE_PHOTO_PERMISSION_GRANTED = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        PICK_IMAGE_PERMISSION_GRANTED = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (canClose())
                finish();
            else
                showConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPositiveDialogResult(int dialogId) {
        if (dialogId == DIALOG_ID_CHANGE_PASSWORD) {
            mSettingsViewModel.setPassword(mSettingsViewModel.getNewPassword().getValue());
            mSettingsViewModel.passwordDialogReset();
        }
    }

    @Override
    public void onNegativeDialogResult(int dialogId) {
    }

    private Uri getImageUri() {
        File imagePath = new File(Environment.getExternalStorageDirectory(), "mik_avatar");
        if (!imagePath.exists())
            if (!imagePath.mkdir())
                return null;
        File imageFile = new File(imagePath, "avatar_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(this, "me.modernpage.makeitknown.provider", imageFile);
    }

    public void processDatePickerResult(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        mSettingsViewModel.setBirthdate(calendar.getTime());
        dataBinding.settinsBirthdateLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.textfield_bg));
    }

    private void showConfirmationDialog() {
        AppDialog dialog = new AppDialog();
        Bundle args = new Bundle();
        args.putInt(AppDialog.DIALOG_ID, DIALOG_ID_CANCEL_EDIT);
        args.putString(AppDialog.DIALOG_MESSAGE, getString(R.string.cancelEditDiag_message));
        args.putInt(AppDialog.DIALOG_POSITIVE_RID, R.string.cancelEditDiag_positive_caption);
        args.putInt(AppDialog.DIALOG_NEGATIVE_RID, R.string.cancelEditDiag_negative_caption);

        dialog.setArguments(args);
        dialog.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onBackPressed() {
        if (canClose())
            super.onBackPressed();
        else
            showConfirmationDialog();
    }

    private boolean canClose() {
        return mSettingsViewModel.canClose();
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void uploadImageClicked() {
            final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            builder.setTitle("Choose your profile picture");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (options[i].equals("Take Photo")) {
                        if (CAPTURE_PHOTO_PERMISSION_GRANTED) {
                            // has permission can continue working
                            mCapturedImageUri = getImageUri();
                            mCaptureImageForResult.launch(mCapturedImageUri);
                        } else {
                            Snackbar.make(dataBinding.getRoot(), "Can't capture photo, grant permission", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Grant Access", view -> {
                                        if (ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this, Manifest.permission.CAMERA) &&
                                                ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                            mUploadImageRequestPermissionForResult
                                                    .launch(new String[]{Manifest.permission.CAMERA,
                                                            Manifest.permission.WRITE_EXTERNAL_STORAGE});
                                        } else {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", SettingsActivity.this.getPackageName(), null);
                                            intent.setData(uri);
                                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            SettingsActivity.this.startActivity(intent);
                                        }
                                    }).show();
                        }
                    } else if (options[i].equals("Choose from Gallery")) {
                        if (PICK_IMAGE_PERMISSION_GRANTED) {
                            mPickImageForResult.launch("image/*");
                        } else {
                            Snackbar.make(dataBinding.getRoot(), "Can't load image, grant Permission", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Grant Access", view -> {
                                        if (ActivityCompat.shouldShowRequestPermissionRationale(SettingsActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                            mUploadImageRequestPermissionForResult
                                                    .launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
                                        } else {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", SettingsActivity.this.getPackageName(), null);
                                            intent.setData(uri);
                                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            SettingsActivity.this.startActivity(intent);
                                        }
                                    }).show();
                        }
                    } else if (options[i].equals("Cancel")) {
                        dialogInterface.dismiss();
                    }
                }
            });
            builder.show();
        }

        @Override
        public void resetPasswordClicked() {
            final long profileId = getIntent().getExtras().getLong(Profile.class.getSimpleName());
            if (profileId == 0)
                return;
            ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();
            Bundle args = new Bundle();
            args.putInt(BaseDialog.DIALOG_ID, DIALOG_ID_CHANGE_PASSWORD);
            args.putLong(Profile.class.getSimpleName(), profileId);
            changePasswordDialog.setArguments(args);
            changePasswordDialog.show(getSupportFragmentManager(), null);
        }

        @Override
        public void setBirthdateClicked() {
            DialogFragment dialogFragment = new DatePickerDialog();
            if (mSettingsViewModel.getBirthdate().getValue() != null) {
                Bundle args = new Bundle();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(mSettingsViewModel.getBirthdate().getValue());
                args.putInt("year", calendar.get(Calendar.YEAR));
                args.putInt("month", calendar.get(Calendar.MONTH));
                args.putInt("day", calendar.get(Calendar.DAY_OF_MONTH));
                dialogFragment.setArguments(args);
            }
            dialogFragment.show(getSupportFragmentManager(), null);
        }

        @Override
        public void updateProfileClicked() {
            long profileId = getIntent().getExtras().getLong(Profile.class.getSimpleName());
            mSettingsViewModel.updateProfile(profileId);
        }
    };
}
