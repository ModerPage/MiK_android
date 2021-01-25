package me.modernpage.util;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Constants {
    private static final String TAG = "Constants";

    private Constants() {
    }

    public static class Gecode {
        public static final String EXTRA_RESULT_RECEIVER = "result_receiver";
        public static final String EXTRA_FETCH_TYPE = "fetch_type";
        public static final int USE_ADDRESS_NAME = 100;
        public static final int USE_ADDRESS_LOCATION = 101;
        public static final String EXTRA_LOCATION_NAME_DATA = "location_name_data";
        public static final String EXTRA_LOCATION_LATLNG = "location_data";

        public static final int FAILURE_RESULT = 200;
        public static final int SUCCESS_RESULT = 201;

        public static final String RESULT_ADDRESS = "result_address";
        public static final String RESULT_MESSAGE = "result_message";

    }

    public static class Network {
        private static final String IP_ADDRESS = "http://192.168.1.108";
        public static final String BASE_URL = IP_ADDRESS + ":8080/MakeItKnown";
        public static final String UPDATEUSER_URL = BASE_URL + "/user/updateUser";
        public static final String UPLOADIMAGE_URL = BASE_URL + "/image/uploadImage";
        public static final String LOGIN_URL = BASE_URL + "/login";
        public static final String REGISTER_URL = BASE_URL + "/register";
        public static final String GETUSER_URL = BASE_URL + "/user/getUser";
        public static final String GETALLGROUP_URL = BASE_URL + "/group/getAllGroups";
        public static final String POSTUPLOADFILE_URL = BASE_URL + "/post/createPost";
        public static final String GETPOSTS_URL = BASE_URL + "/post/getPosts";
        public static final String ADDLIKE_URL = BASE_URL + "/post/addLike";
    }

    public static class Regex {
        public static final String USERNAME_REGEX = "^(?=.*[A-Za-z0-9+_.])(?=\\S+$).{6,15}$";
        public static final String FULLNAME_REGEX = "^[A-Za-z].{8,20}$";
        public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
        public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[[a-zA-Z]])(?=\\S+$).{8,20}$";

        public static boolean isNotValid(String field, String regex) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(field);
            return !matcher.matches();
        }
    }

    public static class User {
        public static final String USERNAME_EXTRA = "loggedin_username_extra";
        public static final String CURRENT_USER_EXTRA = "current_user_extra";
        public static final String LOGIN_REMEMBER_EXTRA = "rememberme";
        public static final String GROUP_EXTRA = "group_extra";
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        Log.d(TAG, "hideKeyboard: view: " + view);
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
