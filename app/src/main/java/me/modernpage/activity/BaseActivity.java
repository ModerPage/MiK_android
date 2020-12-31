package me.modernpage.activity;


import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BaseActivity extends AppCompatActivity {
    private static final String IP_ADDRESS = "http://192.168.1.108";
    public static final String BASE_URL = IP_ADDRESS + ":8080/MakeItKnown";
    public static final String UPDATEUSER_URL = BASE_URL + "/user/updateUser";
    public static final String UPLOADIMAGE_URL = BASE_URL + "/image/uploadImage";
    public static final String LOGIN_URL = BASE_URL + "/login";
    public static final String REGISTER_URL = BASE_URL + "/register";
    public static final String GETUSER_URL = BASE_URL + "/user/getUser";
    public static final String LOGIN_REMEMBER_EXTRA = "rememberme";
    public static final String GETALLGROUP_URL = BASE_URL + "/group/getAllGroups";

    public static final String USERNAME_EXTRA = "loggedin_username_extra";

//    credentials regex
    public static final String USERNAME_REGEX = "^(?=.*[A-Za-z0-9+_.])(?=\\S+$).{6,15}$";
    public static final String FULLNAME_REGEX = "^[A-Za-z].{8,20}$";
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[[a-zA-Z]])(?=\\S+$).{8,20}$";


    public static final String CURRENT_USER_EXTRA = "current_user_extra";

    /*
    * check user credentials (username, password, email, ...) based on regex
    * */
    public boolean isNotValid(String field, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(field);
        return !matcher.matches();
    }
}
