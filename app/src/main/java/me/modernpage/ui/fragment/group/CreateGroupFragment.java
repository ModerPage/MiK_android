package me.modernpage.ui.fragment.group;

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
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;

import dagger.hilt.android.AndroidEntryPoint;
import me.modernpage.activity.R;
import me.modernpage.activity.databinding.FragmentCreateGroupBinding;
import me.modernpage.data.local.entity.PrivateGroup;
import me.modernpage.data.local.entity.Profile;
import me.modernpage.ui.BaseFragment;
import me.modernpage.ui.addedit.GroupAddEditActivity;
import me.modernpage.ui.addedit.GroupAddEditViewModel;
import me.modernpage.ui.common.PostRecyclerViewScrollListener;
import me.modernpage.ui.login.LoginActivity;
import me.modernpage.util.AutoClearedValue;
import me.modernpage.util.FileUtils;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateGroupFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
public class CreateGroupFragment extends BaseFragment<GroupAddEditViewModel, FragmentCreateGroupBinding> {

    private static final String TAG = "CreateGroupFragment";
    private long uid;
    private Uri mCapturedImageUri;
    private static boolean CAPTURE_PHOTO_PERMISSION_GRANTED = false;
    private static boolean PICK_IMAGE_PERMISSION_GRANTED = false;
    private AutoClearedValue<CreateGroupListAdapter> adapter;

    private final ActivityResultLauncher<Uri> mCropImageForResult = registerForActivityResult(
            new CropImageContract(), result -> {
                if (result != null) {
                    viewModel.setUploadedImage(result);
                    ParcelFileDescriptor parcelFD = null;
                    try {
                        parcelFD = getActivity().getContentResolver().openFileDescriptor(result, "r");
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
                        dataBinding.createGroupImage.setImageBitmap(bitmap);
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
    public void onStart() {
        super.onStart();
        CAPTURE_PHOTO_PERMISSION_GRANTED = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        PICK_IMAGE_PERMISSION_GRANTED = ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


    public CreateGroupFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param url create group url.
     * @return A new instance of fragment CreateGroupFragment.
     */
    public static CreateGroupFragment newInstance(String url, long uid) {
        CreateGroupFragment fragment = new CreateGroupFragment();
        Bundle args = new Bundle();
        args.putString(GroupAddEditActivity.EXTRA_URL, url);
        args.putLong(Profile.class.getSimpleName(), uid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.fragment_create_group;
    }

    @Override
    public Class<GroupAddEditViewModel> getViewModel() {
        return GroupAddEditViewModel.class;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uid = getArguments().getLong(Profile.class.getSimpleName());
            if (uid == 0) {
                throw new IllegalArgumentException("Uid not present in bundle.");
            }
        } else
            throw new IllegalArgumentException("Uid must passed in bundle.");
        viewModel.setUid(uid);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dataBinding.setViewModel(viewModel);
        dataBinding.setHandler(mHandler);
        CreateGroupListAdapter createGroupListAdapter = new CreateGroupListAdapter(dataBindingComponent);
        adapter = new AutoClearedValue<>(this, createGroupListAdapter);
        dataBinding.createGroupFollowerList.setAdapter(adapter.get());
        dataBinding.setCallback(() -> viewModel.refresh());
        dataBinding.createGroupSwipeRefresh.setOnRefreshListener(() -> viewModel.pullToRefresh());

        viewModel.getCreateGroupState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                dataBinding.setProcessState(false);
            } else {
                dataBinding.setProcessState(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (state.isVerified()) {
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                    } else {
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", v -> logout()).show();
                    }
                }
                Boolean data = state.getData();
                if (data != null && data) {
                    getActivity().finish();
                }
            }
            dataBinding.executePendingBindings();
        });

        dataBinding.createGroupFollowerList.addOnScrollListener(new PostRecyclerViewScrollListener() {
            @Override
            public void onItemIsFirstVisibleItem(int index) {
            }

            @Override
            public void onItemIsLastPosition(int index) {
                if (index == adapter.get().getItemCount() - 1) {
                    viewModel.loadFollowersNextPage();
                }
            }
        });

        viewModel.getFollowers().observe(getViewLifecycleOwner(), result -> {
            dataBinding.setLoadResource(result);
            int resultCount = (result == null || result.data == null) ? 0 : result.data.getContents().size();
            dataBinding.setResultCount(resultCount);
            adapter.get().replace(result == null || result.data == null ? null : result.data.getContents());
            dataBinding.executePendingBindings();
        });

        viewModel.getLoadMoreStatus().observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                dataBinding.setLoadingMore(false);
            } else {
                dataBinding.setLoadingMore(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (state.isVerified())
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", v -> logout()).show();
                }
            }
            dataBinding.executePendingBindings();
        });

        viewModel.getRefreshState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) {
                dataBinding.createGroupSwipeRefresh.setRefreshing(false);
            } else {
                dataBinding.createGroupSwipeRefresh.setRefreshing(state.isRunning());
                String error = state.getErrorMessageIfNotHandled();
                if (error != null) {
                    if (state.isVerified())
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(dataBinding.getRoot(), error, Snackbar.LENGTH_INDEFINITE)
                                .setAction("LOGOUT", v -> logout()).show();
                }
            }
            dataBinding.executePendingBindings();
        });
    }

    private Uri getImageUri() {
        File imagePath = new File(Environment.getExternalStorageDirectory(), "mik_group_image");
        if (!imagePath.exists())
            if (!imagePath.mkdir())
                return null;
        File imageFile = new File(imagePath, "group_image_" + System.currentTimeMillis() + ".jpg");
        return FileProvider.getUriForFile(getContext(), "me.modernpage.makeitknown.provider", imageFile);
    }

    private void logout() {
        viewModel.relogin();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        getActivity().finish();
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void onUploadImage() {
            final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA) &&
                                                ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                            mUploadImageRequestPermissionForResult
                                                    .launch(new String[]{Manifest.permission.CAMERA,
                                                            Manifest.permission.WRITE_EXTERNAL_STORAGE});
                                        } else {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                            intent.setData(uri);
                                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            getActivity().startActivity(intent);
                                        }
                                    }).show();
                        }
                    } else if (options[i].equals("Choose from Gallery")) {
                        if (PICK_IMAGE_PERMISSION_GRANTED) {
                            mPickImageForResult.launch("image/*");
                        } else {
                            Snackbar.make(dataBinding.getRoot(), "Can't load image, grant Permission", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Grant Access", view -> {
                                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                            mUploadImageRequestPermissionForResult
                                                    .launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
                                        } else {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
                                            intent.setData(uri);
                                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            getActivity().startActivity(intent);
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
        public void onSave() {
            Profile currentUser = viewModel.getUser() == null ||
                    viewModel.getUser().getValue() == null ? null : viewModel.getUser().getValue().data;

            if (currentUser == null)
                return;

            String groupName = viewModel.getGroupName().getValue();
            if (groupName == null || groupName.trim().length() == 0) {
                return;
            }
            File groupImage = null;
            Uri imageValue = viewModel.getUploadedImage().getValue();
            if (imageValue != null) {
                try {
                    groupImage = FileUtils.getFileFromUri(getContext(), imageValue);
                } catch (Exception e) {
                    Log.e(TAG, "updateProfileClicked: Exception" + e.getMessage(), e);
                }
            }
            adapter.get().getSelectedUsers().add(currentUser);
            PrivateGroup privateGroup = new PrivateGroup();
            privateGroup.setName(groupName);
            privateGroup.setOwner(currentUser);
            privateGroup.setMembers(adapter.get().getSelectedUsers());
            viewModel.createGroup(privateGroup, groupImage);
        }
    };

    public interface Handler {
        void onUploadImage();

        void onSave();
    }
}