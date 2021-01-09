package me.modernpage.task;

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
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import me.modernpage.entity.Post;

import static me.modernpage.activity.BaseActivity.POSTUPLOADFILE_URL;

public class ProcessPost extends AsyncTask<Post, Void, String> {
    private static final String TAG = "ProcessPost";

    @Override
    protected String doInBackground(Post... posts) {
        Log.d(TAG, "doInBackground: called");
        if (posts == null) {
            Log.d(TAG, "doInBackground: post not found");
            return null;
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            File uploadFile = new File(posts[0].getFileURL());
            HttpPost httpPost = new HttpPost(POSTUPLOADFILE_URL);
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
                Log.d(TAG, "doInBackground: response status: " + response.getCode());
                Log.d(TAG, "doInBackground: " + response.toString());
                final HttpEntity httpEntity = response.getEntity();

                if (httpEntity != null) {
                    Log.d(TAG, "doInBackground: Response content length: " + httpEntity.getContentLength());
                    Log.d(TAG, "doInBackground: content: " + EntityUtils.toString(httpEntity));
                }
                EntityUtils.consume(httpEntity);
            }
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IOException" + e.getMessage(), e);
        } catch (ParseException e) {
            Log.e(TAG, "doInBackground: ParseException: " + e.getMessage(), e);
        }
        return null;
    }

    private JSONObject getJSON(Post post) {

        try {
            JSONObject postJSON = new JSONObject();
            postJSON.put("username", post.getPostOwner().getUsername());
            JSONObject postLocation = new JSONObject();

            if (post.getPostLocation() != null) {
                postLocation.put("longitude", post.getPostLocation().getLongitude());
                postLocation.put("latitude", post.getPostLocation().getLatitude());
                postLocation.put("addressLine", post.getPostLocation().getAddressLine());
                postLocation.put("city", post.getPostLocation().getCity());
                postLocation.put("country", post.getPostLocation().getCountry());
                postJSON.put("postLocation", postLocation);
            }
            postJSON.put("postGroupId", post.getPostGroup().getGroupId());
            postJSON.put("postText", post.getPostText());

            return postJSON;
        } catch (JSONException e) {
            Log.e(TAG, "getJSON: JSONException", e);
        }

        return null;
    }
}
