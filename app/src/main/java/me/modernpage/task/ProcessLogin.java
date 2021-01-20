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
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import me.modernpage.Constants;
import me.modernpage.activity.R;


public class ProcessLogin extends AsyncTask<Object,Void,String> {
    private static final String TAG = "ProcessLogin";
    private WeakReference<Context> mContext = null;
    private WeakReference<ProgressBar> mProgressBar;
    private OnProcessLogin mCallback;

    public interface OnProcessLogin {
        void onProcessLoginFinished(String status);
    }

    public ProcessLogin(Context context) {
        mContext = new WeakReference<>(context);
        mCallback = (OnProcessLogin) context;
    }

    @Override
    protected void onPreExecute() {
        ConstraintLayout layout = ((Activity)mContext.get()).findViewById(R.id.login_layout);
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
        if(objects == null)
            return null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(Constants.Network.LOGIN_URL);
            connection =(HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestProperty("Content-type","application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();

            String jsonString = "{username: "+ objects[0] + ", password: " + objects[1] + "}";
            OutputStream os = connection.getOutputStream();
            os.write(jsonString.getBytes());
            os.close();

            int ch;
            StringBuilder sb = new StringBuilder();
            InputStream is = connection.getInputStream();
            while ((ch = is.read()) != -1) {
                sb.append((char)ch);
            }
            is.close();
            return sb.toString();
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: " + e);
        } finally {
            if (connection != null)
                connection.disconnect();
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
        mCallback.onProcessLoginFinished(s);
    }
}
