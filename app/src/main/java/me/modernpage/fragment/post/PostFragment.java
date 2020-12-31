package me.modernpage.fragment.post;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import me.modernpage.activity.GoogleMapActivity;
import me.modernpage.activity.R;
import me.modernpage.entity.Group;
import me.modernpage.entity.UserEntity;
import me.modernpage.task.GetAllGroup;
import me.modernpage.task.ProcessUser;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static me.modernpage.activity.BaseActivity.USERNAME_EXTRA;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostFragment extends Fragment implements GetAllGroup.OnGetAllGroup, ProcessUser.OnProcessUser {
    private static final String TAG = "PostFragment";
    private static final int TAKEPHOTO_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    private static final int TAKEVIDEO_REQUEST = 2;
    private static final int GALLERY_VIDEO_REQUEST = 3;
    private static final int ERROR_DIALOG_REQUEST = 9001;

    private Spinner mSpinner;
    private ImageView mAvatar;
    private TextView mFullname;
    private LinearLayout mPostFileContainer;

//     this boolean is to ensure that user can upload only one file
    private boolean mIsFileExist = false;

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
        GetAllGroup getAllGroup = new GetAllGroup(getContext(), this);
        getAllGroup.execute();

        String username = getArguments().getString(USERNAME_EXTRA);
//        Log.d(TAG, "onViewCreated: username: " + username);
        mSpinner = view.findViewById(R.id.post_group_list);
        mAvatar = view.findViewById(R.id.post_avatar);
        mFullname = view.findViewById(R.id.post_fullname);
        mPostFileContainer = view.findViewById(R.id.file_container);

        ProcessUser processUser = new ProcessUser(this);
        processUser.execute(username);



        ImageView addPhoto = view.findViewById(R.id.post_add_photo);
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
                                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                startActivityForResult(takePicture, TAKEPHOTO_REQUEST);
                            } else if (options[i].equals("Choose from Gallery")) {
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intent, GALLERY_IMAGE_REQUEST);
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

        ImageView addVideo = view.findViewById(R.id.post_add_video);
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
                                Intent takePicture = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                                startActivityForResult(takePicture, TAKEVIDEO_REQUEST);
                            } else if (options[i].equals("Choose from Gallery")) {
                                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(intent, GALLERY_VIDEO_REQUEST);
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


        ImageView addLocation = view.findViewById(R.id.post_add_location);
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

    private void initGoogleMap() {
        Intent intent = new Intent(getActivity(), GoogleMapActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode != RESULT_CANCELED) {

            final CustomImageLayout imageView = new CustomImageLayout(getContext());
            final CustomVideoLayout videoView = new CustomVideoLayout(getContext());

            imageView.getCloseButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPostFileContainer.removeView(imageView);
                    mIsFileExist = false;
                }
            });

            videoView.getCloseButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPostFileContainer.removeView(videoView);
                    mIsFileExist = false;
                }
            });

            mIsFileExist = true;
            Bitmap selectedBitmap = null;
            switch (requestCode) {
                case TAKEPHOTO_REQUEST:
                    if(resultCode == RESULT_OK && data != null) {
                        selectedBitmap  = (Bitmap) data.getExtras().get("data");
                    }
                    break;
                case GALLERY_IMAGE_REQUEST:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage =  data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContext().getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                                Log.d(TAG, "onActivityResult: bitmap : " + bitmap);
                                selectedBitmap = Bitmap.createScaledBitmap(bitmap ,
                                        (int) (bitmap.getWidth() * 0.1),
                                        (int) (bitmap.getHeight() * 0.1),
                                        true);
                                cursor.close();
                            }
                        }
                    }
                    break;

                case TAKEVIDEO_REQUEST:
                    if(resultCode == RESULT_OK && data != null) {
                        Uri uri = data.getData();
                        videoView.getVideoView().setVideoURI(uri);
                        mPostFileContainer.addView(videoView);
                    }
                    break;
                case GALLERY_VIDEO_REQUEST:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedVideo = data.getData();
                        String[] filePathColumn = {MediaStore.Video.Media.DATA};
                        if(selectedVideo != null) {
                            Cursor cursor = getContext().getContentResolver().query(selectedVideo,
                                    filePathColumn, null, null, null);
                            if(cursor != null) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String videoPath = cursor.getString(columnIndex);
                                videoView.getVideoView().setVideoPath(videoPath);
                                mPostFileContainer.addView(videoView);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
            if(selectedBitmap != null) {
                imageView.getImageView().setImageBitmap(selectedBitmap);
                mPostFileContainer.addView(imageView);
            }
        }
        super.onActivityResult(requestCode,resultCode, data);
    }

    @Override
    public void onGetAllGroupComplete(List<Group> groups) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),R.layout.spinner_item,prepareGroupList(groups));
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
    }

    private String[] prepareGroupList(List<Group> groups) {
        String[] list = new String[groups.size()];
        for(int i=0, length=groups.size(); i<length; i++) {
            list[i] = groups.get(i).getGroupName();
        }
        return list;
    }

    public static PostFragment newInstance(String username) {
        PostFragment fragment = new PostFragment();
        Bundle bundle = new Bundle();
        bundle.putString(USERNAME_EXTRA, username);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onProcessUserFinished(UserEntity user) {
        Log.d(TAG, "onProcessUserFinished: starts");
        Log.d(TAG, "onProcessUserFinished: fullname: " + user.getFullname());
        mFullname.setText(user.getFullname());
        Log.d(TAG, "onProcessUserFinished: imageuri: " + user.getImageUri());
        Picasso.get().load(user.getImageUri())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .into(mAvatar);
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
}
