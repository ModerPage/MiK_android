package me.modernpage.ui.register;

import android.content.Intent;
import android.os.Bundle;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivityRegistrationSuccessBinding;
import me.modernpage.ui.BaseActivity;
import me.modernpage.ui.login.LoginActivity;

public class RegistrationSuccessActivity extends BaseActivity<ActivityRegistrationSuccessBinding> {

    @Override
    public int getLayoutRes() {
        return R.layout.activity_registration_success;
    }

    public interface Handler {
        void onLogin();

        void onOpenEmailApp();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dataBinding.setHandler(new Handler() {
            @Override
            public void onLogin() {
                Intent intent = new Intent(RegistrationSuccessActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onOpenEmailApp() {
                Intent emailIntent = new Intent(Intent.ACTION_MAIN);
                emailIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
                emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(Intent.createChooser(emailIntent, "Send mail using..."));
            }
        });

    }
}