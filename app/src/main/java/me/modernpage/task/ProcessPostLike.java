package me.modernpage.task;

import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import me.modernpage.entity.Like;
import me.modernpage.util.Constants;

public class ProcessPostLike {
    private static final String TAG = "ProcessPostLike";

    private OnProcessPostLikeListener mCallback;
    private ExecutorService mExecutorService;
    private Handler mMainThreadHandler;

    public interface OnProcessPostLikeListener {
        void onAddPostLikeComplete(Like addedLike);

        void onDeletePostLikeComplete(Like deletedLike);
    }

    public ProcessPostLike(OnProcessPostLikeListener callback, ExecutorService executorService,
                           Handler mainThreadHandler) {
        mCallback = callback;
        mExecutorService = executorService;
        mMainThreadHandler = mainThreadHandler;
    }

    public void addLike(final Like like) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                Like addedLike = doInBackgroundAddLike(like);
                notifyResultAddLike(addedLike);
            }
        });
    }

    public void deleteLike(final Like like) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                Like deletedLike = doInBackgroundDeleteLike(like);
                notifyResultDeleteLike(deletedLike);
            }
        });
    }

    private Like doInBackgroundAddLike(Like like) {
        if (like == null)
            return null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(Constants.Network.ADDLIKE_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "application/json");
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();
            OutputStream outputStream = connection.getOutputStream();
            String requestBody = addJsonRequestBody(like);
            outputStream.write(requestBody.getBytes());
            outputStream.close();

            if (connection.getResponseCode() == 201) {
                StringBuilder result = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                    result.append(line).append("\n");
                }
                bufferedReader.close();
                return getLikeFromJSON(result.toString());
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: MalformedURLException " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IOException " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return null;
    }

    private Like doInBackgroundDeleteLike(Like like) {
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
            osw.write(deleteJsonRequestBody(like));
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

    private void notifyResultAddLike(Like like) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null)
                    mCallback.onAddPostLikeComplete(like);
            }
        });
    }

    private void notifyResultDeleteLike(Like like) {
        Log.d(TAG, "notifyResult: like: " + like);
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null)
                    mCallback.onDeletePostLikeComplete(like);
            }
        });
    }

    private String addJsonRequestBody(Like like) {
        JSONObject jsonLike = new JSONObject();
        try {
            jsonLike.put("userEmail", like.getLikeOwner().getEmail());
            jsonLike.put("postId", like.getLikedPost().getPostId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonLike.toString();
    }

    private String deleteJsonRequestBody(Like like) {
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
