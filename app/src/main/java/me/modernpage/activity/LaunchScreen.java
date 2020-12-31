package me.modernpage.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class LaunchScreen extends BaseActivity {
    private static final String TAG = "LaunchScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch_screen);

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                    Intent intent = new Intent(LaunchScreen.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                } catch (InterruptedException e) {
                    Log.e(TAG, "run: Launch screen error: " + e.getMessage());
                }
            }
        };

        thread.start();
    }
}
