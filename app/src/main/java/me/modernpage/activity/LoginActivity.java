package me.modernpage.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;


import me.modernpage.util.Constants;
import me.modernpage.task.ProcessLogin;


public class LoginActivity extends AppCompatActivity implements ProcessLogin.OnProcessLogin {
    private static final String TAG = "LoginActivity";

    private static final String LOGGEDIN = "loggedin";
    private static final String USER_NOTEXIST = "usernotexist";
    private static final String INCORRECT_DATA = "incorrectdata";

    private static final String LOGIN_USERNAME = "login_username";
    private static final String LOGIN_PASSWORD = "login_password";

    private static boolean isRemembered = false;
    private CheckBox mCheckBox;

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(Constants.User.LOGIN_REMEMBER_EXTRA, isRemembered);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isRemembered = savedInstanceState.getBoolean(Constants.User.LOGIN_REMEMBER_EXTRA);
        mCheckBox.setChecked(isRemembered);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button signin = findViewById(R.id.login_signin);
        mCheckBox = findViewById(R.id.login_checkbox);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        isRemembered = sharedPreferences.getBoolean(Constants.User.LOGIN_REMEMBER_EXTRA, false);
        if(isRemembered) {
            mCheckBox.setChecked(true);
            String username = sharedPreferences.getString(LOGIN_USERNAME,null);
            String password = sharedPreferences.getString(LOGIN_PASSWORD,null);
            EditText username_edt = findViewById(R.id.login_username);
            username_edt.setText(username);

            EditText password_edt = findViewById(R.id.login_password);
            password_edt.setText(password);
        }


        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText username_edt = findViewById(R.id.login_username);
                EditText password_edt = findViewById(R.id.login_password);
                boolean isValidLogin = true;

                if(isBlank(username_edt.getText().toString())) {
                    isValidLogin = false;
                    username_edt.setError("the field can't be blank");
                } else if (Constants.Regex.isNotValid(username_edt.getText().toString().trim(), Constants.Regex.USERNAME_REGEX)) {
                    isValidLogin = false;
                    username_edt.setError("username must be 6 characters long at least and can contain \".\" \"_\" chars");
                }

                if(isBlank(password_edt.getText().toString())) {
                    password_edt.setError("the field can't be blank");
                } else if (Constants.Regex.isNotValid(password_edt.getText().toString().trim(), Constants.Regex.PASSWORD_REGEX)) {
                    isValidLogin = false;
                    password_edt.setError("password must be 8 characters long at least and contain a digit");
                }

                if(isValidLogin) {
                    Log.d(TAG, "onClick: username: " + username_edt.getText().toString() + ", password: " + password_edt.getText().toString());
                    ProcessLogin processLogin = new ProcessLogin(LoginActivity.this);
                    processLogin.execute(username_edt.getText().toString().trim().toLowerCase(),password_edt.getText().toString().trim());
                }
            }
        });

    }

    public void rememberCheck(View view) {
        if(isRemembered)
            mCheckBox.setChecked(false);
        else
            mCheckBox.setChecked(true);
        isRemembered = mCheckBox.isChecked();
    }

    @Override
    public void onProcessLoginFinished(String status) {
        if(status == null) {
            Toast.makeText(this,"occurs something wrong", Toast.LENGTH_SHORT).show();
        } else {
            if (LOGGEDIN.equals(status)) {
                /*
                 * if "remember me" is checked, then save user login data to remember
                 * */
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if(isRemembered) {
                    EditText usernameEt = findViewById(R.id.login_username);
                    EditText passwordEt = findViewById(R.id.login_password);
                    sharedPreferences.edit().putString(LOGIN_USERNAME,usernameEt.getText().toString().trim()).
                            putString(LOGIN_PASSWORD,passwordEt.getText().toString().trim()).apply();
                    sharedPreferences.edit().putBoolean(Constants.User.LOGIN_REMEMBER_EXTRA, isRemembered).apply();
                }

                //goto home page
                EditText usernameEt = findViewById(R.id.login_username);
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(Constants.User.USERNAME_EXTRA, usernameEt.getText().toString().trim());
                startActivity(intent);
                finish();
            }
            if (USER_NOTEXIST.equals(status)) {
                Toast.makeText(this,"such user does not exist",Toast.LENGTH_LONG).show();
            }
            if (INCORRECT_DATA.equals(status)) {
                Toast.makeText(this,"user data is incorrect",Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean isBlank(String text) {
        return text.trim().length() == 0;
    }

    public void goToSignup(View view) {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }
}
