package me.modernpage.ui.launch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import me.modernpage.activity.R;
import me.modernpage.ui.login.LoginActivity;

public class LaunchScreen extends AppCompatActivity {
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
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(LaunchScreen.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });

                } catch (InterruptedException e) {
                    Log.e(TAG, "run: Launch screen error: " + e.getMessage());
                }
            }
        };

        thread.start();
    }
}
