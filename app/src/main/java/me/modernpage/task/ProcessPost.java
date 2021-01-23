package me.modernpage.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.FileBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.modernpage.util.Constants;
import me.modernpage.activity.MainActivity;
import me.modernpage.entity.Location;
import me.modernpage.entity.Post;


public class ProcessPost extends AsyncTask<Post, Void, Post> {
    private static final String TAG = "ProcessPost";
    private WeakReference<Context> mContext;
    private OnProcessPost mCallback;

    public interface OnProcessPost {
        void onProcessPostComplete(Post post);
    }

    public ProcessPost(Context context, OnProcessPost callback) {
        mContext = new WeakReference<>(context);
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        ((MainActivity) mContext.get()).startProgressBar();
    }

    @Override
    protected Post doInBackground(Post... posts) {
        Log.d(TAG, "doInBackground: called");
        if (posts == null) {
            Log.d(TAG, "doInBackground: post not found");
            return null;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            File uploadFile = new File(posts[0].getFileURL());
            HttpPost httpPost = new HttpPost(Constants.Network.POSTUPLOADFILE_URL);
            FileBody fileBody = new FileBody(uploadFile);
            StringBody title = new StringBody("fileName: " + uploadFile.getName(), ContentType.TEXT_PLAIN);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addPart("file", fileBody);
            builder.addPart("title", title);
            builder.addPart("postContent", new StringBody(getJSON(posts[0]).toString(), ContentType.APPLICATION_JSON));
            HttpEntity entity = builder.build();

            httpPost.setEntity(entity);

            Log.d(TAG, "doInBackground: executing request: ");
            try (final CloseableHttpResponse response = httpClient.execute(httpPost)) {
                if (response.getCode() == 201) {
                    HttpEntity responseEntity = response.getEntity();
                    StringBuilder result = new StringBuilder();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(responseEntity.getContent()));
                    for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                        result.append(line).append("\n");
                    }
                    Log.d(TAG, "doInBackground: response result: " + result.toString());
                    reader.close();
                    return getPostFromJSON(result.toString());
                }

            }
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IOException" + e.getMessage(), e);
        }
        return null;
    }

    private Post getPostFromJSON(String jsonResponse) {
        try {
            JSONObject postJson = new JSONObject(jsonResponse);
            long postId = postJson.getLong("postId");

            Object location = postJson.get("postLocation");
            Location postLocation = null;
            if (!JSONObject.NULL.equals(location)) {
                double longitude = ((JSONObject) location).getDouble("longitude");
                double latitude = ((JSONObject) location).getDouble("latitude");
                String addressLine = ((JSONObject) location).getString("addressLine");
                String city = ((JSONObject) location).getString("city");
                String country = ((JSONObject) location).getString("country");
                postLocation = new Location(longitude, latitude, addressLine, city, country);
            }

            String postText = postJson.getString("postText");
            String fileUri = postJson.getString("fileUri");

            String date = postJson.getString("created");
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
            Date postDate = dateFormat.parse(date);

            Post post = new Post();
            post.setPostId(postId);
            post.setPostLocation(postLocation);
            post.setPostText(postText);
            post.setFileURL(fileUri);
            post.setPostedDate(postDate);
            return post;
        } catch (JSONException e) {
            Log.e(TAG, "getPostFromJSON: JSONException: " + e.getMessage(), e);
        } catch (ParseException e) {
            Log.e(TAG, "getPostFromJSON: ParseException: " + e.getMessage(), e);
        }
        return null;
    }
    private JSONObject getJSON(Post post) {

        try {
            JSONObject postJSON = new JSONObject();
            postJSON.put("username", post.getPostOwner().getUsername());
            JSONObject postLocation = null;

            if (post.getPostLocation() != null) {
                postLocation = new JSONObject();
                postLocation.put("longitude", post.getPostLocation().getLongitude());
                postLocation.put("latitude", post.getPostLocation().getLatitude());
                postLocation.put("addressLine", post.getPostLocation().getAddressLine());
                postLocation.put("city", post.getPostLocation().getCity());
                postLocation.put("country", post.getPostLocation().getCountry());
            }

            postJSON.put("postLocation", postLocation == null ? JSONObject.NULL : postLocation);
            postJSON.put("postGroupId", post.getPostGroup().getGroupId());
            postJSON.put("postText", post.getPostText());

            return postJSON;
        } catch (JSONException e) {
            Log.e(TAG, "getJSON: JSONException", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Post post) {
        ((MainActivity) mContext.get()).stopProgressBar();

        if (post != null && mCallback != null) {
            mCallback.onProcessPostComplete(post);
        }
    }
}
