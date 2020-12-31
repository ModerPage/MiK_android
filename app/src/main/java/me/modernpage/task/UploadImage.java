package me.modernpage.task;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import me.modernpage.activity.R;

import static me.modernpage.activity.BaseActivity.BASE_URL;
import static me.modernpage.activity.BaseActivity.UPLOADIMAGE_URL;

public class UploadImage extends AsyncTask<Bitmap,Void,String> {
    private static final String TAG = "UploadImage";
    private WeakReference<Context> mContext;
    private WeakReference<ProgressBar> mProgressBar;
    private OnUploadeImage mCallback;
    private String mUsername;
    public interface OnUploadeImage{
        void onUploadImageFinished(String status);
    }

    public UploadImage(Context context, String currentUsername) {
        mContext = new WeakReference<>(context);
        mCallback = (OnUploadeImage) context;
        mUsername = currentUsername;
    }

    @Override
    protected void onPreExecute() {
        ConstraintLayout layout = ((Activity)mContext.get()).findViewById(R.id.settings_layout);
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
        mProgressBar.get().setVisibility(View.VISIBLE);
        ((Activity)mContext.get()).getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    @Override
    protected void onPostExecute(String s) {
        mProgressBar.get().setVisibility(View.INVISIBLE);
        ((Activity)mContext.get()).getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        mCallback.onUploadImageFinished(s);

    }

    @Override
    protected String doInBackground(Bitmap... bitmaps) {
        if(bitmaps == null)
            return null;
        HttpURLConnection connection;
        try {
            URL url = new URL(UPLOADIMAGE_URL);
            connection =(HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestProperty("Content-type","stream");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            bitmaps[0].compress(Bitmap.CompressFormat.JPEG,90,bao);
            byte[] bytes = bao.toByteArray();
            String base64 = Base64.encodeToString(bytes,Base64.DEFAULT);
            base64 = mUsername + "," + base64;
            connection.connect();

            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(base64);
            osw.close();

            int ch;
            StringBuilder sb = new StringBuilder();
            InputStream is = connection.getInputStream();
            while ((ch = is.read()) != -1) {
                sb.append((char)ch);
            }
            is.close();
            return sb.toString();

        } catch (IOException e) {
            Log.e(TAG, "doInBackground: " + e );
        }
        return null;
    }
}
