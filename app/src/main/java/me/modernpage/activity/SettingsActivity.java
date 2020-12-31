package me.modernpage.activity;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.modernpage.entity.UserEntity;
import me.modernpage.fragment.DatePickerFragment;
import me.modernpage.task.UpdateUser;
import me.modernpage.task.UploadImage;

public class SettingsActivity extends BaseActivity implements UpdateUser.OnUpdateUser, UploadImage.OnUploadeImage {
    private static final int TAKEPHOTO_REQUEST = 0;
    private static final int GALLERY_REQUEST = 1;
    private static final String IMAGE_UPLOADED = "image_uploaded";
    private static final String USERNAME_EXIST = "username_exist";
    private static final String USER_UPDATED = "user_updated";
    private static final String TAG = "SettingsActivity";
    private ImageView mPhoto;

    private TextView mBirthdate_tw;
    private TextInputLayout mFullname;
    private TextInputLayout mUsername;
    private TextInputLayout mPassword;
    private TextInputLayout mEmail;
    private Date mBirthdate;
    private UserEntity mCurrentUser;

    private String tempUsername;
    private boolean mLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        mBirthdate_tw = findViewById(R.id.settings_birthdate);
        mFullname = findViewById(R.id.settings_fullname);
        mUsername = findViewById(R.id.settings_username);
        mPassword = findViewById(R.id.settings_password);
        mEmail = findViewById(R.id.settings_email);
        mPhoto = findViewById(R.id.settings_image);

        Intent intent = getIntent();
        mCurrentUser = (UserEntity) intent.getExtras().get(CURRENT_USER_EXTRA);
        if(mCurrentUser != null) {
            mUsername.getEditText().setText(mCurrentUser.getUsername());
            mFullname.getEditText().setText(mCurrentUser.getFullname());
            mEmail.getEditText().setText(mCurrentUser.getEmail());
            mBirthdate = mCurrentUser.getBirthdate();
            if(mBirthdate != null) {
                mBirthdate_tw.setText(mBirthdate.toString());
            }
            if(mCurrentUser.getImageUri() != null) {
                Picasso.get().load( mCurrentUser.getImageUri())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(mPhoto);
            }
        }
    }

    public void processDatePickerResult(int year, int month, int day) {
        String birthdate = "%1s-%2s-%3s";
        mBirthdate_tw.setText(String.format(birthdate,day,month,year));
        try {
            mBirthdate =  new SimpleDateFormat("dd-MM-yyyy", Locale.US).parse(mBirthdate_tw.getText().toString());
        } catch (ParseException e) {
            Log.e(TAG, "processDatePickerResult: " + e.getMessage());
        }
    }

    public void pickBirthdate(View view) {
        DialogFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getSupportFragmentManager(),"pick birthdate dialog");
    }

    public void selectImage(View view) {
        final CharSequence[] options = {"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
        builder.setTitle("Choose your profile picture");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(options[i].equals("Take Photo")) {
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture,TAKEPHOTO_REQUEST);
                } else if(options[i].equals("Choose from Gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, GALLERY_REQUEST);
                } else if(options[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode != RESULT_CANCELED) {
            Bitmap selectedBitmap = null;
            switch (requestCode) {
                case TAKEPHOTO_REQUEST:
                    if(resultCode == RESULT_OK && data != null) {
                        selectedBitmap = (Bitmap) data.getExtras().get("data");

                    }
                    break;
                case GALLERY_REQUEST:
                    if (resultCode == RESULT_OK && data != null) {
                        Uri selectedImage =  data.getData();
                        String[] filePathColumn = {MediaStore.Images.Media.DATA};
                        if (selectedImage != null) {
                            Cursor cursor = getContentResolver().query(selectedImage,
                                    filePathColumn, null, null, null);
                            if (cursor != null) {
                                cursor.moveToFirst();

                                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                                String picturePath = cursor.getString(columnIndex);
                                Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
                                Log.d(TAG, "onActivityResult: bitmap : " + bitmap);

                                // scale image to reduce size
                                selectedBitmap = Bitmap.createScaledBitmap(bitmap ,
                                        (int) (bitmap.getWidth() * 0.1),
                                        (int) (bitmap.getHeight() * 0.1),
                                        true);
                                cursor.close();
                            }
                        }
                    }
                    break;
            }
            if(selectedBitmap != null) {
                mPhoto.setImageBitmap(selectedBitmap);
                UploadImage uploadImage = new UploadImage(SettingsActivity.this, mCurrentUser.getUsername());
                uploadImage.execute(selectedBitmap);
            }
        }
        super.onActivityResult(requestCode,resultCode, data);
    }

    public void saveSettings(View view) {

        boolean validSettings = true;
        String fullname = mFullname.getEditText().getText().toString();
        if(isBlank(fullname)) {
            mFullname.setError("the field can't be empty");
            validSettings = false;
        } else if(isNotValid(fullname.trim(), FULLNAME_REGEX)) {
            validSettings = false;
            mFullname.setError("full name must be 8 characters long at least");
        }

        String username = mUsername.getEditText().getText().toString();
        if(isBlank(username)) {
            mUsername.setError("the field can't be empty");
            validSettings = false;
        } else if(isNotValid(username.trim(), USERNAME_REGEX)) {
            mUsername.setError("username must be 6 characters long at least and can contain \".\" \"_\" chars");
            validSettings = false;
        }

        String password = mPassword.getEditText().getText().toString();
        if(!isBlank(password)) {
            if(isNotValid(password.trim(), PASSWORD_REGEX)) {
                mPassword.setError("password must be 8 characters long at least and contain a digit");
                validSettings = false;
            }
        }

        if(validSettings && null != mBirthdate) {
            if(!mCurrentUser.getFullname().equals(fullname.trim())
                    || !mCurrentUser.getUsername().equals(username.trim())
                    || mBirthdate.compareTo(mCurrentUser.getBirthdate()==null ? new Date()
                    : mCurrentUser.getBirthdate()) != 0
                    || password.trim().length() > 0) {


                if(!mCurrentUser.getUsername().equals(username.trim())) {
                    tempUsername = mCurrentUser.getUsername();
                    mCurrentUser.setUsername(username.trim());
                    mLogin = true;
                }

                if(password.trim().length() > 0) {
                    mCurrentUser.setPassword(password.trim());
                    mLogin = true;
                }

                mCurrentUser.setFullname(fullname.trim());
                mCurrentUser.setBirthdate(mBirthdate);

                UpdateUser updateUser = new UpdateUser(SettingsActivity.this);
                updateUser.execute(mCurrentUser);

            } else {
                finish();
            }
        }
    }

    private boolean isBlank(String text) {
        return text.trim().length() == 0;
    }

    @Override
    public void onUpdateUserFinished(String status) {
        if(USER_UPDATED.equals(status)) {
            Toast.makeText(this,"User personal information updated successfully", Toast.LENGTH_SHORT).show();
            if(mLogin) {
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                sharedPreferences.edit().remove(LOGIN_REMEMBER_EXTRA).apply();

                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else
                finish();

        } else if(USERNAME_EXIST.equals(status)) {
            mCurrentUser.setUsername(tempUsername);
            Toast.makeText(this,"The username exist, type another one", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this,"Failed while updating user personal information", Toast.LENGTH_SHORT).show();
        }
        mCurrentUser.setBirthdate(null);
    }

    @Override
    public void onUploadImageFinished(String status) {
        if(status == null || status.trim().equals(""))
            Toast.makeText(this,"occurs something wrong", Toast.LENGTH_SHORT).show();
        else if(IMAGE_UPLOADED.equals(status)) {
//              image successfully uploaded
            Toast.makeText(this,"Image uploaded successfully", Toast.LENGTH_LONG).show();
        }
    }
}
