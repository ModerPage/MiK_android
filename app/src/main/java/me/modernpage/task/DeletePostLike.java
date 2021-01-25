package me.modernpage.task;

import android.os.AsyncTask;
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

import me.modernpage.entity.Like;
import me.modernpage.util.Constants;

public class DeletePostLike extends AsyncTask<Like, Void, Like> {
    private static final String TAG = "DeletePostLike";
    private OnDeletePostLike mCallback;

    public interface OnDeletePostLike {
        void onDeletePostLikeComplete(Like removedLike);
    }

    public DeletePostLike(OnDeletePostLike callback) {
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Like removedLike) {
        if (mCallback != null)
            mCallback.onDeletePostLikeComplete(removedLike);
    }

    @Override
    protected Like doInBackground(Like... likes) {
        if (likes[0] == null)
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
            osw.write(jsonRequestBody(likes[0]));
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
