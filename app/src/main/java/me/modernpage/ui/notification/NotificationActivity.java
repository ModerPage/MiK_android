package me.modernpage.ui.notification;

import android.os.Bundle;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivityNotificationBinding;
import me.modernpage.ui.BaseActivity;

public class NotificationActivity extends BaseActivity<ActivityNotificationBinding> {

    @Override
    public int getLayoutRes() {
        return R.layout.activity_notification;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}