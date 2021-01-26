package me.modernpage.task;

import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import me.modernpage.entity.Like;
import me.modernpage.util.Constants;

public class DeletePostLike {
    private static final String TAG = "DeletePostLike";
    private OnDeletePostLike mCallback;
    private ExecutorService mExecutorService;
    private Handler mMainThreadHandler;

    public interface OnDeletePostLike {
        void onDeletePostLikeComplete(Like removedLike);
    }

    public DeletePostLike(OnDeletePostLike callback, ExecutorService executorService, Handler handler) {
        mCallback = callback;
        mExecutorService = executorService;
        mMainThreadHandler = handler;
    }

    public void deleteLike(final Like like) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                Like deletedLike = doInBackground(like);
                notifyResult(deletedLike);
            }
        });
    }

    private void notifyResult(Like like) {
        Log.d(TAG, "notifyResult: like: " + like);
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null)
                    mCallback.onDeletePostLikeComplete(like);
            }
        });
    }

    private Like doInBackground(Like like) {
        if (like == null)
            return null;

        HttpURLConnection connection = null;
        try {
            URL url = new URL(Constants.Network.DELETELIKE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("DELETE");
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();
            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(jsonRequestBody(like));
            osw.close();

            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
            char[] buffer = new char[128];
            StringBuilder result = new StringBuilder();
            int charsRead;
            while ((charsRead = isr.read(buffer)) != -1) {
                result.append(buffer, 0, charsRead);
            }
            isr.close();
            return getLikeFromJSON(result.toString());

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IOException: " + e.getMessage(), e);
        } finally {
            if (connection != null)
                connection.disconnect();
        }

        return null;
    }

    private String jsonRequestBody(Like like) {
        JSONObject jsonLike = new JSONObject();
        try {
            Log.d(TAG, "jsonRequestBody: like: " + like);
            jsonLike.put("likeId", like.getLikeId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonLike.toString();
    }

    private Like getLikeFromJSON(String jsonString) {
        try {
            JSONObject jsonLike = new JSONObject(jsonString);
            long likeId = jsonLike.getLong("likeId");
            Date likedDate = Constants.mDateFormat.parse((String) jsonLike.get("likedDate"));
            Like like = new Like();
            like.setLikeId(likeId);
            like.setLikedDate(likedDate);
            return like;
        } catch (JSONException e) {
            Log.e(TAG, "getLikeCountFromJSON: JSONException: " + e.getMessage(), e);
        } catch (ParseException e) {
            Log.e(TAG, "getLikeCountFromJSON: ParseException: " + e.getMessage(), e);
        }
        return null;
    }
}
