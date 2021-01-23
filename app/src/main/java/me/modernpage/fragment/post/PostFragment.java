package me.modernpage.fragment.post;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.modernpage.util.Constants;
import me.modernpage.util.PermissionUtils;
import me.modernpage.activity.GoogleMapActivity;
import me.modernpage.activity.MainActivity;
import me.modernpage.activity.R;
import me.modernpage.entity.Group;
import me.modernpage.entity.Location;
import me.modernpage.entity.Post;
import me.modernpage.entity.UserEntity;
import me.modernpage.task.ProcessPost;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements ProcessPost.OnProcessPost {
    private static final String TAG = "PostFragment";

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 10;
    private static final int READ_PERMISSION_REQUEST_CODE = 11;

    private static final int TAKEPHOTO_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    private static final int TAKEVIDEO_REQUEST = 2;
    private static final int GALLERY_VIDEO_REQUEST = 3;
    private static final int ERROR_DIALOG_REQUEST = 9001;
    private static final int ADD_LOCATION_REQUEST = 4;

    private static final String LOADED_IMAGE_EXTRA = "loaded_bitmap";
    private static final String LOADED_VIDEO_EXTRA = "loaded_video";
    private static final String LOADED_ADDRESS_EXTRA = "loaded_address";
    private static final String IS_FILE_EXIST_EXTRA = "is_file_exist";
    private static final String GROUP_INDEX_EXTRA = "selected_group";

    private Spinner mSpinner;
    private ImageView mAvatar;
    private TextView mFullname;
    private EditText mContent;
    private TextView mPostLocation;

    private LinearLayout mPostFileContainer;
    private CustomImageLayout mImageLayout;
    private CustomVideoLayout mVideoLayout;

    private String mLoadedImagePath;
    private String mLoadedVideoPath;
    private Address mLoadedAddress;
    private int mGroupIndex = -1;
    private boolean mSpinnerInitial = true;
    private UserEntity mCurrentUser;
    private List<Group> mGroups;

//     this boolean is to ensure that user can upload only one file
    private boolean mIsFileExist = false;

    private OnAddedNewPost mCallback;

    public interface OnAddedNewPost {
        void onAddedNewPost(Post post);

    }

    public PostFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_post, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: starts");

        mCallback = (OnAddedNewPost) getActivity().getSupportFragmentManager().findFragmentByTag("1");
        mSpinner = view.findViewById(R.id.post_group_list);
        mAvatar = view.findViewById(R.id.post_avatar);
        mFullname = view.findViewById(R.id.post_fullname);
        mPostFileContainer = view.findViewById(R.id.file_container);
        mPostLocation = view.findViewById(R.id.post_location);
        Button postButton = view.findViewById(R.id.post_post);

        if (getArguments() != null) {
            Log.d(TAG, "onViewCreated: getArguments() not null");

            mCurrentUser = (UserEntity) getArguments().getSerializable(Constants.User.CURRENT_USER_EXTRA);
            mGroups = (ArrayList<Group>) getArguments().getSerializable(Constants.User.GROUP_EXTRA);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, prepareGroupList(mGroups));
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(adapter);
            mSpinner.setSelection(mGroupIndex);
            mFullname.setText(mCurrentUser.getFullname());

            Picasso.get().load(Constants.Network.BASE_URL + mCurrentUser.getImageUri())
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(mAvatar);
        }

        if (savedInstanceState != null) {
            mGroupIndex = savedInstanceState.getInt(GROUP_INDEX_EXTRA);
            mIsFileExist = savedInstanceState.getBoolean(IS_FILE_EXIST_EXTRA);
            Log.d(TAG, "onViewCreated: isFileExist: " + mIsFileExist);
            if (mIsFileExist) {
                mLoadedImagePath = savedInstanceState.getString(LOADED_IMAGE_EXTRA);
                Log.d(TAG, "onViewCreated: mLoadedImagePath: " + mLoadedImagePath);
                mLoadedVideoPath = savedInstanceState.getString(LOADED_VIDEO_EXTRA);
                Log.d(TAG, "onViewCreated: mLoadedVideoPath: " + mLoadedVideoPath);
            }

            mLoadedAddress = savedInstanceState.getParcelable(LOADED_ADDRESS_EXTRA);
            Log.d(TAG, "onViewCreated: mLoadedAddress: " + mLoadedAddress);
        }


        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContent = getView().findViewById(R.id.post_content);
                String post_content = mContent.getText().toString().trim();
                String post_file_path = null;
                Location post_location = null;
                Group post_group = mGroups.get(mGroupIndex < 0 ? 0 : mGroupIndex);

                if (post_content.length() == 0) {
                    mContent.setError("Post text can't be empty");
                    return;
                }

                if (!mIsFileExist) {
                    Toast.makeText(getContext(), "Upload an image or a video to make a post", Toast.LENGTH_LONG).show();
                    return;
                } else {
                    if (mLoadedImagePath != null) {
                        post_file_path = mLoadedImagePath;
                    } else if (mLoadedVideoPath != null) {
                        post_file_path = mLoadedVideoPath;
                    }
                }

                if (mLoadedAddress != null)
                    post_location = new Location(mLoadedAddress.getLongitude(),
                            mLoadedAddress.getLatitude(), mLoadedAddress.getAddressLine(0),
                            mLoadedAddress.getLocality(), mLoadedAddress.getCountryName());

                Post post = new Post(mCurrentUser, post_location, post_group, post_content, post_file_path);
                ProcessPost processPost = new ProcessPost(getContext(), PostFragment.this);
                processPost.execute(post);
            }
        });

        mImageLayout = new CustomImageLayout(getContext());
        mVideoLayout = new CustomVideoLayout(getContext());

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (mSpinnerInitial) {
                    mSpinnerInitial = false;
                    return;
                }
                Log.d(TAG, "onItemSelected: position: " + position + ", " + mGroupIndex);
                if (mGroupIndex != position) {
                    mGroupIndex = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        mImageLayout.getCloseButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPostFileContainer.removeView(mImageLayout);
                mIsFileExist = false;
                mLoadedImagePath = null;
            }
        });

        mVideoLayout.getCloseButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mPostFileContainer.removeView(mVideoLayout);
                mIsFileExist = false;
                mLoadedVideoPath = null;
            }
        });


        ImageView addPhoto = getView().findViewById(R.id.post_add_photo);
        addPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mIsFileExist) {
                    final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Choose your post picture");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (options[i].equals("Take Photo")) {
                                Log.d(TAG, "onClick: Take Photo");
                                if (PermissionUtils.hasPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        && PermissionUtils.hasPermission(getActivity(), Manifest.permission.CAMERA)) {
                                    Log.d(TAG, "onClick: hasPermission");
                                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, setImageUri());
                                    startActivityForResult(takePicture, TAKEPHOTO_REQUEST);
                                } else {
                                    if (PermissionUtils.shouldAskForPermission(getActivity(), Manifest.permission.CAMERA)
                                            && PermissionUtils.shouldAskForPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                        Log.d(TAG, "onClick: shouldAskForPermission");
                                        PermissionUtils.requestPermissions(PostFragment.this,
                                                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                CAMERA_PERMISSION_REQUEST_CODE);
                                    } else {
                                        Toast.makeText(getContext(), "Go to the app settings to enable permissions", Toast.LENGTH_LONG).show();
                                    }
                                }
                            } else if (options[i].equals("Choose from Gallery")) {
                                Log.d(TAG, "onClick: Choose from Gallery");
                                if (PermissionUtils.hasPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    Log.d(TAG, "onClick: hasPermission");
                                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(intent, GALLERY_IMAGE_REQUEST);
                                } else {
                                    if (PermissionUtils.shouldAskForPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                        Log.d(TAG, "onClick: shouldAskForPermission");
                                        PermissionUtils.requestPermissions(PostFragment.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                READ_PERMISSION_REQUEST_CODE);
                                    } else
                                        Toast.makeText(getContext(), "Go to the app settings to enable permission", Toast.LENGTH_LONG).show();
                                }
                            } else if (options[i].equals("Cancel"))
                                dialogInterface.dismiss();
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(getContext(), "You can post only one file", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView addVideo = getView().findViewById(R.id.post_add_video);
        addVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mIsFileExist) {
                    final CharSequence[] options = {"Take Video", "Choose from Gallery", "Cancel"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Choose your post video");
                    builder.setItems(options, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (options[i].equals("Take Video")) {
                                Log.d(TAG, "onClick: Take Video");
                                if (PermissionUtils.hasPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        && PermissionUtils.hasPermission(getActivity(), Manifest.permission.CAMERA)) {
                                    Log.d(TAG, "onClick: hasPermission");
                                    Intent takeVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                    startActivityForResult(takeVideo, TAKEVIDEO_REQUEST);
                                } else {
                                    if (PermissionUtils.shouldAskForPermission(getActivity(), Manifest.permission.CAMERA)
                                            && PermissionUtils.shouldAskForPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                        Log.d(TAG, "onClick: shouldAskForPermission");
                                        PermissionUtils.requestPermissions(PostFragment.this,
                                                new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                CAMERA_PERMISSION_REQUEST_CODE);
                                    } else {
                                        Toast.makeText(getContext(), "Go to the app settings to enable permissions", Toast.LENGTH_LONG).show();
                                    }
                                }
                            } else if (options[i].equals("Choose from Gallery")) {
                                Log.d(TAG, "onClick: Choose from Gallery");
                                if (PermissionUtils.hasPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                    Log.d(TAG, "onClick: hasPermission");
                                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                                    startActivityForResult(intent, GALLERY_VIDEO_REQUEST);
                                } else {
                                    if (PermissionUtils.shouldAskForPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                                        Log.d(TAG, "onClick: shouldAskForPermission");
                                        PermissionUtils.requestPermissions(PostFragment.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                                READ_PERMISSION_REQUEST_CODE);
                                    } else
                                        Toast.makeText(getContext(), "Go to the app settings to enable permission", Toast.LENGTH_LONG).show();
                                }
                            } else if (options[i].equals("Cancel")) {
                                dialogInterface.dismiss();
                            }
                        }
                    });
                    builder.show();
                } else {
                    Toast.makeText(getContext(), "You can post only one file", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView addLocation = getView().findViewById(R.id.post_add_location);
        addLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: add location pressed");
                if(isGoogleServiceOK()) {
                    initGoogleMap();
                }
            }
        });

        Log.d(TAG, "onViewCreated: ends");
    }

    private Uri setImageUri() {
        File imagePath = new File(Environment.getExternalStorageDirectory(), "DCIM");
        File imageFile = new File(imagePath, "captured_post_image" + new Date().getTime() + ".jpg");
        Uri imageUri = FileProvider.getUriForFile(getContext(), "me.modernpage.makeitknown.provider", imageFile);
        this.mLoadedImagePath = imageFile.getAbsolutePath();
        return imageUri;
    }

    private void initGoogleMap() {
        Intent intent = new Intent(getActivity(), GoogleMapActivity.class);
        getActivity().startActivityForResult(intent, ADD_LOCATION_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult: called");

        if(resultCode != RESULT_CANCELED) {
            final int unmaskedRequestCode = requestCode & 0x0000ffff;
            Log.d(TAG, "onActivityResult: unmaskedRequestCode: " + unmaskedRequestCode);

            switch (unmaskedRequestCode) {
                case ADD_LOCATION_REQUEST:
                    if (resultCode == RESULT_OK && data != null) {
                        mLoadedAddress = data.getParcelableExtra(GoogleMapActivity.LAST_ADDRESS_EXTRA);
                        Log.d(TAG, "onActivityResult: ADD_LOCATION_REQUEST: " + mLoadedAddress);
                    }
                    break;

                case TAKEPHOTO_REQUEST:
                    if (resultCode == RESULT_OK)
                        mIsFileExist = true;
                    break;

                case GALLERY_IMAGE_REQUEST:
                    if (resultCode == RESULT_OK && data != null) {
                        mIsFileExist = true;
                        Log.d(TAG, "onActivityResult: unmaskedRequestCode: " + unmaskedRequestCode);
                        Uri selectedImage = data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContext().getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                mLoadedImagePath = cursor.getString(columnIndex);
                                cursor.close();
                            }
                        }
                    }
                    break;

                case TAKEVIDEO_REQUEST:
                    if(resultCode == RESULT_OK && data != null) {
                        mIsFileExist = true;
                        Log.d(TAG, "onActivityResult: unmaskedRequestCode: " + unmaskedRequestCode);
                        Uri uri = data.getData();
                        Log.d(TAG, "onActivityResult: uri" + uri + ", uri path: " + uri.getPath());
                        mLoadedVideoPath = uri.getPath();
                    }
                    break;

                case GALLERY_VIDEO_REQUEST:
                    if (resultCode == RESULT_OK && data != null) {
                        mIsFileExist = true;
                        Log.d(TAG, "onActivityResult: unmaskedRequestCode: " + unmaskedRequestCode);
                        Uri selectedVideo = data.getData();
                        String[] filePathColumn = {MediaStore.Video.Media.DATA};
                        if (selectedVideo != null) {
                            Cursor cursor = getContext().getContentResolver().query(selectedVideo,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                mLoadedVideoPath = cursor.getString(columnIndex);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
        }
        super.onActivityResult(requestCode,resultCode, data);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: called");
        outState.putBoolean(IS_FILE_EXIST_EXTRA, mIsFileExist);
        outState.putInt(GROUP_INDEX_EXTRA, mGroupIndex);
        if (mIsFileExist) {
            if (mLoadedImagePath != null)
                outState.putString(LOADED_IMAGE_EXTRA, mLoadedImagePath);
            if (mLoadedVideoPath != null)
                outState.putString(LOADED_ADDRESS_EXTRA, mLoadedVideoPath);
        }

        if (mLoadedAddress != null)
            outState.putParcelable(LOADED_ADDRESS_EXTRA, mLoadedAddress);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: called");

        if (mIsFileExist && mPostFileContainer.getChildCount() == 0) {
            Log.d(TAG, "onResume: mLoadedImagePath: " + mLoadedImagePath);
            Log.d(TAG, "onResume: mLodadedVideoPath: " + mLoadedVideoPath);
            if (mLoadedImagePath != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(mLoadedImagePath);
                bitmap = Bitmap.createScaledBitmap(bitmap,
                        (int) (bitmap.getWidth() * 0.1),
                        (int) (bitmap.getHeight() * 0.1),
                        true);
                mImageLayout.getImageView().setImageBitmap(bitmap);
                mPostFileContainer.addView(mImageLayout);
            } else if (mLoadedVideoPath != null) {
                mVideoLayout.getVideoView().setVideoPath(mLoadedVideoPath);
                mPostFileContainer.addView(mVideoLayout);
            }
        }

        if (mLoadedAddress != null)
            mPostLocation.setText(mLoadedAddress.getAddressLine(0));

        if (getActivity() != null)
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        super.onResume();
    }

    @Override
    public void onPause() {
        if (getActivity() != null)
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        super.onPause();
    }

    private String[] prepareGroupList(List<Group> groups) {
        List<String> arrayList = new ArrayList<>();
        for(int i=0, length=groups.size(); i<length; i++) {
            if (!"private".equals(groups.get(i).getGroupType().getGroupTypeName()))
                arrayList.add(groups.get(i).getGroupName());
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }

    public static PostFragment newInstance(UserEntity currentUser, List<Group> currentGroups) {
        PostFragment fragment = new PostFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.User.CURRENT_USER_EXTRA, currentUser);
        bundle.putSerializable(Constants.User.GROUP_EXTRA, (ArrayList<Group>) currentGroups);
        fragment.setArguments(bundle);
        return fragment;
    }

    private boolean isGoogleServiceOK() {
        Log.d(TAG, "isGoogleServiceOK: checking google service version");
        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());
        if(available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isGoogleServiceOK: Google Plat Service is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isGoogleServiceOK: error occured but you can resolve it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(),available, ERROR_DIALOG_REQUEST);
            dialog.show();
        } else {
            Log.d(TAG, "isGoogleServiceOK: error occured with google service");
            Toast.makeText(getContext(), "You can't make map requests", Toast.LENGTH_LONG).show();
        }
        
        return false;
    }

    @Override
    public void onProcessPostComplete(Post post) {
        Log.d(TAG, "onProcessPostComplete: post: " + post);
        if (post != null) {
            // reset and go to home page
            // post is created
            post.setPostOwner(mCurrentUser);
            post.setPostGroup(mGroups.get(mGroupIndex < 0 ? 0 : mGroupIndex));
            Fragment homeFragment = getActivity().getSupportFragmentManager().findFragmentByTag("1");
            ((MainActivity) getActivity()).moveFragment(this, homeFragment, R.id.nav_home);
            mCallback.onAddedNewPost(post);
            reset();
        } else {
            Toast.makeText(getContext(), "Error occurs while procession the post", Toast.LENGTH_LONG).show();
        }
    }

    private void reset() {
        mPostFileContainer.removeAllViews();
        mIsFileExist = false;
        mLoadedVideoPath = null;
        mLoadedImagePath = null;
        mPostLocation.setText("");
        mLoadedAddress = null;
        mSpinner.setSelection(0);
        mGroupIndex = -1;
        mSpinnerInitial = true;
        mContent.getText().clear();

    }
}
