package me.modernpage.task;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import me.modernpage.Constants;
import me.modernpage.activity.R;


public class RegisterUser extends AsyncTask<Object,Void,String>  {
    private static final String TAG = "RegisterUser";
    private WeakReference<ProgressBar> mProgressBar;
    private WeakReference<Context> mContext;
    private OnRegisterUser mCallback;

    public interface OnRegisterUser {
        void onRegisterUserFinished(String status);
    }

    public RegisterUser(Context context) {
        mContext = new WeakReference<>(context);
        mCallback = (OnRegisterUser) context;
    }

    @Override
    protected void onPreExecute() {
        ConstraintLayout layout = ((Activity)mContext.get()).findViewById(R.id.register_layout);
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,ConstraintLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.bottomToBottom = ConstraintSet.PARENT_ID;
        layoutParams.endToEnd = ConstraintSet.PARENT_ID;
        layoutParams.startToStart = ConstraintSet.PARENT_ID;
        layoutParams.topToTop = ConstraintSet.PARENT_ID;

        ProgressBar progressBar = new ProgressBar(mContext.get(),null,android.R.attr.progressBarStyle);
        progressBar.setLayoutParams(layoutParams);
        progressBar.setVisibility(View.INVISIBLE);
        layout.addView(progressBar);
        mProgressBar = new WeakReference<>(progressBar);

        ((Activity)mContext.get()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    protected String doInBackground(Object... objects) {
        HttpURLConnection connection = null;
        if(objects == null) {
            return null;
        }
        try {
            URL url = new URL(Constants.Network.REGISTER_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestProperty("Content-type","stream");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();

            ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
            oos.writeObject(objects[0]);
            oos.close();

            int ch;
            StringBuilder sb = new StringBuilder();
            InputStream is = connection.getInputStream();
            while ((ch = is.read()) != -1) {
                sb.append((char)ch);
            }
            is.close();
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        mProgressBar.get().setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPostExecute(String s) {

        mProgressBar.get().setVisibility(View.INVISIBLE);
        ((Activity)mContext.get()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mCallback.onRegisterUserFinished(s);
    }
}
