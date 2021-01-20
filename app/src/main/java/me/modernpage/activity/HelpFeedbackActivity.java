package me.modernpage.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.net.URI;
import java.net.URL;

import me.modernpage.Constants;
import me.modernpage.entity.UserEntity;

public class HelpFeedbackActivity extends AppCompatActivity {
    private UserEntity mCurrentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_feedback);

        Intent intent = getIntent();
        mCurrentUser = (UserEntity) intent.getExtras().get(Constants.User.CURRENT_USER_EXTRA);
    }

    public void onSend(View view) {
        EditText title = findViewById(R.id.helpfeedback_title);
        String title_text = title.getText().toString().trim();
        boolean isValid = true;
        if(title_text.length() == 0) {
            title.setError("Title can't be blank");
            isValid = false;
        }

        EditText description = findViewById(R.id.helpfeedback_description);
        String description_text = description.getText().toString().trim();

        if(description_text.length() == 0) {
            description.setError("Description can't be blank");
            isValid = false;
        }

        if(!isValid) {
            return;
        }

        Uri uri = Uri.parse("mailto:my.genteel@list.ru")
                .buildUpon()
                .appendQueryParameter("subject", title_text)
                .appendQueryParameter("body", description_text)
                .build();

        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
        if(intent.resolveActivity(getPackageManager()) != null)
            startActivity(intent);
    }
}
