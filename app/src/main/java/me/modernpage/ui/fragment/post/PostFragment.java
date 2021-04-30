package me.modernpage.ui.fragment.post;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.FragmentPostBinding;
import me.modernpage.data.local.entity.Group;
import me.modernpage.data.local.entity.LoadResult;
import me.modernpage.data.local.entity.Location;
import me.modernpage.data.local.entity.Post;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.data.local.entity.relation.PostRelation;
import me.modernpage.ui.BaseFragment;
import me.modernpage.ui.googlemap.GoogleMapActivity;
import me.modernpage.ui.login.LoginActivity;
import me.modernpage.util.FileUtils;
import me.modernpage.util.Objects;
import me.modernpage.util.Status;

@AndroidEntryPoint
public class PostFragment extends BaseFragment<PostFragmentViewModel, FragmentPostBinding> {
    private static final String TAG = "PostFragment";
    private static final int ERROR_DIALOG_REQUEST = 101;
    private static boolean CAMERA_CAPTURE_PERMISSION_GRANTED = false;
    private static boolean FILE_PICK_PERMISSION_GRANTED = false;
    public static final String EXTRA_GROUP = "group";
    private String postLoadResultId;

    public enum FragmentEditMode {ADD, EDIT}

    private FragmentEditMode mMode;

    private File mCapturedImageFile;
    private File mCapturedVideoFile;

    private OnSaveClicked mOnSaveClicked;

    public interface OnSaveClicked {
        void onSaveClicked();
    }

    public interface Handler {
        void onAddFileClicked();

        void onAddImageClicked();

        void onAddVideoClicked();

        void onAddLocationClicked();

        void onPostClicked();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = getActivity();
        if (!(activity instanceof OnSaveClicked)) {
            throw new ClassCastException(activity.getClass().getSimpleName() +
                    "must implement PostFragment.OnSaveClicked interface");
        }
        mOnSaveClicked = (OnSaveClicked) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnSaveClicked = null;
    }

    private final ActivityResultLauncher<Uri> mCaptureImageForResult = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), result -> {
                if (result && mCapturedImageFile != null) {
                    viewModel.setFile(mCapturedImageFile);
                }
            }
    );

    private final ActivityResultLauncher<Uri> mCaptureVideoForResult = registerForActivityResult(
            new CaptureVideo(), result -> {
                if (result && mCapturedVideoFile != null) {
                    viewModel.setFile(mCapturedVideoFile);
                }
            }
    );

    private final ActivityResultLauncher<String> mPickMediaFileForResult = registerForActivityResult(
            new ActivityResultContracts.GetContent(), result -> {
                if (result != null) {
                    viewModel.setFile(getFileFromUri(result));
                }
            }
    );

    private final ActivityResultLauncher<String[]> mUploadFileRequestPermissionForResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (result.containsKey(Manifest.permission.CAMERA) && result.containsKey(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    CAMERA_CAPTURE_PERMISSION_GRANTED = result.get(Manifest.permission.CAMERA) && result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (result.containsKey(Manifest.permission.READ_EXTERNAL_STORAGE))
                    FILE_PICK_PERMISSION_GRANTED = result.get(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
    );

    private final ActivityResultLauncher<Void> mGoogleMapForResult = registerForActivityResult(
            new GoogleMapContract(), result -> {
                // check for result
                if (result != null) {
                    Location location = new Location();
                    location.setAddressLine(result.getAddressLine(0));
                    location.setLongitude(result.getLongitude());
                    location.setLatitude(result.getLatitude());
                    location.setCity(result.getLocality());
                    location.setCountry(result.getCountryName());
                    viewModel.setLocation(location);
                }
            }
    );

    private static class GoogleMapContract extends ActivityResultContract<Void, Address> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Void input) {
            return new Intent(context, GoogleMapActivity.class);
        }

        @Override
        public Address parseResult(int resultCode, @Nullable Intent intent) {
            return intent == null ? null : intent.getParcelableExtra(Address.class.getSimpleName());
        }
    }

    private static class CaptureVideo extends ActivityResultContract<Uri, Boolean> {

        @NonNull
        @Override
        public Intent createIntent(@NonNull Context context, Uri input) {
            return new Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                    .putExtra(MediaStore.EXTRA_OUTPUT, input);
        }

        @Override
        public Boolean parseResult(int resultCode, @Nullable Intent intent) {
            return resultCode == Activity.RESULT_OK;
        }
    }

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_post;
    }

    @Override
    public Class<PostFragmentViewModel> getViewModel() {
        return PostFragmentViewModel.class;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        long uid;
        PostRelation post;
        if (args != null) {
            uid = args.getLong(Profile.class.getSimpleName());
            postLoadResultId = args.getString(LoadResult.class.getSimpleName());
            post = (PostRelation) args.getSerializable(PostRelation.class.getSimpleName());
            if (post != null) {
                mMode = FragmentEditMode.EDIT;
                viewModel.setPost(post);
                viewModel.setText(post.getPost().getText());
                viewModel.setFile(null);
                dataBinding.setFileUrl(post.getPost()._file());
                viewModel.setLocation(post.getLocation());
                dataBinding.setUser(post.getOwner());
                dataBinding.setGroups(Collections.singletonList(post.getGroup()));
                dataBinding.postGroupList.setEnabled(false);
            } else if (uid > 0 && postLoadResultId != null) {
                mMode = FragmentEditMode.ADD;
                viewModel.setUid(uid);
                viewModel.getUser().observe(getViewLifecycleOwner(), userEntity -> {
                    if (userEntity.status == Status.SUCCESS) {
                        dataBinding.setUser(userEntity.data);
                    } else if (userEntity.status == Status.LOGOUT) {
                        Snackbar.make(dataBinding.getRoot(), userEntity.message, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", v -> logout()).show();
                    } else if (userEntity.status == Status.ERROR) {
                        Snackbar.make(dataBinding.getRoot(), userEntity.message, Snackbar.LENGTH_LONG).show();
                    }
                    dataBinding.executePendingBindings();
                });

                Group group = (Group) args.getSerializable(EXTRA_GROUP);
                if (group != null) {
                    dataBinding.setGroups(Collections.singletonList(group));
                    dataBinding.postGroupList.setEnabled(false);
                } else {
                    viewModel.getGroups().observe(getViewLifecycleOwner(), groups -> {
                        if (groups == null)
                            return;

                        if (groups.status == Status.SUCCESS) {
                            dataBinding.setGroups(groups.data == null ? null : groups.data.getContents());
                        } else if (groups.status == Status.LOGOUT) {
                            Snackbar.make(dataBinding.getRoot(), groups.message, Snackbar.LENGTH_INDEFINITE)
                                    .setAction("LOGOUT", v -> {
                                        logout();
                                    }).show();
                        } else if (groups.status == Status.ERROR) {
                            Snackbar.make(dataBinding.getRoot(), groups.message, Snackbar.LENGTH_LONG).show();
                        }
                        dataBinding.executePendingBindings();
                    });
                }
            } else {
                throw new IllegalArgumentException("User id and/or post load url not present in bundle.");
            }
        } else {
            throw new IllegalArgumentException("User id and/or post load url must pass in bundle.");
        }
        dataBinding.setEditMode(mMode);
        dataBinding.setViewModel(viewModel);
        dataBinding.setHandler(mHandler);
        dataBinding.executePendingBindings();

        viewModel.getProcessState().observe(getViewLifecycleOwner(), processState -> {
            if (processState == null) {
                dataBinding.setProcessState(false);
            } else {
                dataBinding.setProcessState(processState.isRunning());
                String error = processState.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (processState.isVerified())
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", v -> logout()).show();
                }
                Boolean data = processState.getData();
                if (data != null && data) {
                    if (mOnSaveClicked != null) {
                        viewModel.reset();
                        mOnSaveClicked.onSaveClicked();
                        dataBinding.postFile.setImageResource(R.drawable.add_file_image);
                    }
                }
            }
            dataBinding.executePendingBindings();
        });
    }

    private void logout() {
        viewModel.relogin();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        CAMERA_CAPTURE_PERMISSION_GRANTED = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        FILE_PICK_PERMISSION_GRANTED = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static PostFragment newInstance(long uid, String postLoadUrl) {
        PostFragment postFragment = new PostFragment();
        Bundle args = new Bundle();
        args.putLong(Profile.class.getSimpleName(), uid);
        args.putString(LoadResult.class.getSimpleName(), postLoadUrl);
        postFragment.setArguments(args);
        return postFragment;
    }

    private File getFileFromUri(Uri uri) {
        try {
            return FileUtils.getFileFromUri(getContext(), uri);
        } catch (Exception e) {
            Log.e(TAG, "getFileFromUri: Exception: " + e.getMessage(), e);
        }
        return null;
    }

    private boolean isGoogleServiceOK() {
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());
        if (available == ConnectionResult.SUCCESS) {
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Toast.makeText(getContext(), "You can't make map requests", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void showImageFileDialog() {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Choose your profile picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (options[i].equals("Take Photo")) {
                    if (CAMERA_CAPTURE_PERMISSION_GRANTED) {
                        // has permission can continue working
                        mCaptureImageForResult.launch(getImageUri());
                    } else {
                        Snackbar.make(dataBinding.getRoot(), "Can't capture photo, grant permission", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Grant Access", view -> {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA) &&
                                            ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                        mUploadFileRequestPermissionForResult
                                                .launch(new String[]{Manifest.permission.CAMERA,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE});
                                    } else {
                                        appSettings();
                                    }
                                }).show();
                    }
                } else if (options[i].equals("Choose from Gallery")) {
                    Log.d(TAG, "onClick: pick_image_permission: " + FILE_PICK_PERMISSION_GRANTED);
                    if (FILE_PICK_PERMISSION_GRANTED) {
                        mPickMediaFileForResult.launch("image/*");
                    } else {
                        Snackbar.make(dataBinding.getRoot(), "Can't load image, grant Permission", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Grant Access", view -> {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                        mUploadFileRequestPermissionForResult
                                                .launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
                                    } else {
                                        appSettings();
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

    private void appSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
        intent.setData(uri);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private Uri getImageUri() {
        File imagePath = new File(Environment.getExternalStorageDirectory(), "mik_post_image");
        if (!imagePath.exists())
            if (!imagePath.mkdirs())
                return null;
        mCapturedImageFile = new File(imagePath, "post_image_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(getContext(), "me.modernpage.makeitknown.provider", mCapturedImageFile);
    }

    private Uri getVideoUri() {
        File videoPath = new File(Environment.getExternalStorageDirectory(), "mik_post_video");
        if (!videoPath.exists())
            if (!videoPath.mkdir())
                return null;
        mCapturedVideoFile = new File(videoPath, "post_video_" + System.currentTimeMillis() + ".mp4");
        return FileProvider.getUriForFile(getContext(), "me.modernpage.makeitknown.provider", mCapturedVideoFile);
    }

    private void showVideoFileDialog() {
        final CharSequence[] options = {"Take Video", "Choose from Gallery", "Cancel"};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Choose your post video");
        builder.setItems(options, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (options[i].equals("Take Video")) {
                    if (CAMERA_CAPTURE_PERMISSION_GRANTED) {
                        mCaptureVideoForResult.launch(getVideoUri());
                    } else {
                        Snackbar.make(dataBinding.getRoot(), "Can't capture video, grant permission", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Grant Access", view -> {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA) &&
                                            ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                        mUploadFileRequestPermissionForResult
                                                .launch(new String[]{Manifest.permission.CAMERA,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE});
                                    } else {
                                        appSettings();
                                    }
                                }).show();
                    }
                } else if (options[i].equals("Choose from Gallery")) {
                    if (FILE_PICK_PERMISSION_GRANTED) {
                        mPickMediaFileForResult.launch("video/*");
                    } else {
                        Snackbar.make(dataBinding.getRoot(), "Can't load video, grant Permission", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Grant Access", view -> {
                                    if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                        mUploadFileRequestPermissionForResult
                                                .launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
                                    } else {
                                        appSettings();
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

    private final Handler mHandler = new Handler() {
        @Override
        public void onAddFileClicked() {
            final CharSequence[] options = {"Photo", "Video", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Select post file to upload");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (options[i].equals("Photo")) {
                        // show image dialog
                        showImageFileDialog();
                    } else if (options[i].equals("Video")) {
                        // show video dialog
                        showVideoFileDialog();
                    }
                    dialogInterface.dismiss();
                }
            });

            builder.show();
        }

        @Override
        public void onAddImageClicked() {
            showImageFileDialog();
        }

        @Override
        public void onAddVideoClicked() {
            showVideoFileDialog();
        }

        @Override
        public void onAddLocationClicked() {
            if (isGoogleServiceOK()) {
                mGoogleMapForResult.launch(null);
            }
        }

        @Override
        public void onPostClicked() {

            if (mMode == FragmentEditMode.ADD) {
                File postFile = viewModel.getFile().getValue();
                if (postFile == null) {
                    Toast.makeText(getContext(), "No file uploaded", Toast.LENGTH_LONG).show();
                    return;
                }
                Profile user = dataBinding.getUser();
                List<Group> groups = dataBinding.getGroups();
                String postText = viewModel.getText().getValue();
                Location location = viewModel.getLocation().getValue();
                if (postText == null || postText.trim().length() == 0) {
                    Toast.makeText(getContext(), "Add post text", Toast.LENGTH_LONG).show();
                    return;
                }
                if (user != null && groups != null) {
                    Post post = new Post();
                    post.setOwner(user);
                    post.setGroup(groups.get(viewModel.getGroupIndex().getValue() == null ? 0 :
                            viewModel.getGroupIndex().getValue()));
                    post.setText(postText);
                    post.setLocation(location);
                    viewModel.createPost(postLoadResultId, user._posts(), post, postFile);
                }
            } else {
                PostRelation post = viewModel.getPost();
                if (post != null) {
                    String text = viewModel.getText().getValue();
                    File file = viewModel.getFile().getValue();
                    Location location = viewModel.getLocation().getValue();

                    if (text == null || text.trim().length() == 0) {
                        Toast.makeText(getContext(), "Add post text", Toast.LENGTH_LONG).show();
                        return;
                    }
                    boolean isChanged = false;
                    if (file != null) {
                        isChanged = true;
                    }

                    if (!Objects.equals(post.getPost().getText(), text)) {
                        post.getPost().setText(text);
                        isChanged = true;
                    }
                    if (!Objects.equals(post.getPost().getLocation(), location)) {
                        post.getPost().setLocation(location);
                        isChanged = true;
                    }

                    if (isChanged) {
                        Post p = post.getPost();
                        p.setLocation(post.getLocation());
                        p.setOwner(post.getOwner());
                        p.setGroup(post.getGroup());
                        p.setFile(post.getFile());
                        viewModel.updatePost(p._self(), p, file);
                    }
                }
            }
        }
    };

    public boolean canClose() {
        return false;
    }
}
