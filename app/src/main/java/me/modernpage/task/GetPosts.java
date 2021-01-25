package me.modernpage.task;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.modernpage.util.Constants;
import me.modernpage.entity.Comment;
import me.modernpage.entity.Group;
import me.modernpage.entity.GroupType;
import me.modernpage.entity.Like;
import me.modernpage.entity.Location;
import me.modernpage.entity.Post;
import me.modernpage.entity.UserEntity;

public class GetPosts extends AsyncTask<String, Void, List<Post>> {
    private static final String TAG = "GetPosts";
    private OnGetPosts mCallback;
    private SimpleDateFormat mDateFormat;

    public GetPosts(OnGetPosts callback) {
        mCallback = callback;
        mDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault());
    }

    public interface OnGetPosts {
        void onGetPostsComplete(List<Post> posts);
    }

    @Override
    protected List<Post> doInBackground(String... strings) {

        if (strings == null)
            return null;
        HttpURLConnection httpURLConnection = null;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(Constants.Network.BASE_URL + strings[0]);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setReadTimeout(10000);
            httpURLConnection.setConnectTimeout(15000);
            httpURLConnection.connect();

            StringBuilder result = new StringBuilder();
            bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
            for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                result.append(line).append("\n");
            }
            bufferedReader.close();
            Log.d(TAG, "doInBackground: getposts json result: " + result.toString());
            return getPostsFromJSON(result.toString());
        } catch (MalformedURLException e) {
            Log.d(TAG, "doInBackground: MalformedURLException " + e.getMessage(), e);
        } catch (IOException e) {
            Log.d(TAG, "doInBackground: IOException: " + e.getMessage(), e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Post> posts) {
        mCallback.onGetPostsComplete(posts);
    }

    private List<Post> getPostsFromJSON(String jsonString) {
        List<Post> posts = new ArrayList<>();
        try {
            JSONArray postsJSON = new JSONArray(jsonString);
            for (int i = 0, length = postsJSON.length(); i < length; i++) {

                JSONObject postJSON = (JSONObject) postsJSON.get(i);
                long postId = postJSON.getLong("postId");
                String postText = postJSON.getString("postText");
                String postFileURI = postJSON.getString("fileURI");
                String postedDateText = postJSON.getString("postedDate");
                Date postedDate = mDateFormat.parse(postedDateText);

                JSONObject postOwnerJSON = postJSON.getJSONObject("postOwner");
                String username = postOwnerJSON.getString("username");
                String email = postOwnerJSON.getString("email");
                String imageURI = postOwnerJSON.getString("imageURI");
                UserEntity postOwner = new UserEntity();
                postOwner.setUsername(username);
                postOwner.setEmail(email);
                postOwner.setImageUri(imageURI);

                Object postLocationObject = postJSON.get("postLocation");
                Location postLocation = null;
                Log.d(TAG, "getPostsFromJSON: postlocationObject is null?: " + JSONObject.NULL.equals(postLocationObject));
                if (!JSONObject.NULL.equals(postLocationObject)) {
                    Log.d(TAG, "getPostsFromJSON: postLocationObject: " + postLocationObject);
                    JSONObject postLocationJSON = (JSONObject) postLocationObject;
                    double longitude = postLocationJSON.getDouble("longitude");
                    double latitude = postLocationJSON.getDouble("latitude");
                    String addressLine = postLocationJSON.getString("addressLine");
                    String city = postLocationJSON.getString("city");
                    String country = postLocationJSON.getString("country");
                    postLocation = new Location(longitude, latitude, addressLine, city, country);

                }

                JSONObject postGroupJSON = postJSON.getJSONObject("postGroup");
                long postGroupId = postGroupJSON.getLong("groupId");
                String postGroupName = postGroupJSON.getString("name");
                String postGroupTypeName = postGroupJSON.getString("type");
                Group postGroup = new Group();
                postGroup.setGroupId(postGroupId);
                postGroup.setGroupName(postGroupName);
                GroupType postGroupType = new GroupType();
                postGroupType.setGroupTypeName(postGroupTypeName);
                postGroup.setGroupType(postGroupType);


                JSONArray likesJSON = postJSON.getJSONArray("postLikes");
                List<Like> postLikes = new ArrayList<>();
                for (int j = 0, likeLength = likesJSON.length(); j < likeLength; j++) {
                    JSONObject likeJSON = (JSONObject) likesJSON.get(j);
                    long likeId = likeJSON.getLong("likeId");
                    Date likedDate = mDateFormat.parse((String) likeJSON.get("likedDate"));
                    JSONObject likedUserJSON = likeJSON.getJSONObject("likedUser");
                    String likedUserUsername = likedUserJSON.getString("username");
                    String likedUserEmail = likedUserJSON.getString("email");
                    String likedUserImageURI = likedUserJSON.getString("imageURI");

                    UserEntity likedUser = new UserEntity();
                    likedUser.setUsername(likedUserUsername);
                    likedUser.setEmail(likedUserEmail);
                    likedUser.setImageUri(likedUserImageURI);

                    Like like = new Like();
                    like.setLikeId(likeId);
                    like.setLikedDate(likedDate);
                    like.setLikeOwner(likedUser);

                    postLikes.add(like);
                }

                JSONArray commentsJSON = postJSON.getJSONArray("postComments");
                List<Comment> postComments = new ArrayList<>();
                for (int c = 0, commentLength = commentsJSON.length(); c < commentLength; c++) {
                    JSONObject commentJSON = (JSONObject) commentsJSON.get(c);
                    long commentId = commentJSON.getLong("commentId");
                    String commentText = commentJSON.getString("commentText");
                    Date commentDate = mDateFormat.parse((String) commentJSON.get("commentDate"));

                    JSONObject commentedUserJSON = commentJSON.getJSONObject("commentedUser");
                    String commentedUserUsername = commentedUserJSON.getString("username");
                    String commentedUsetEmail = commentedUserJSON.getString("email");
                    String commentedUserImageURI = commentedUserJSON.getString("imageURI");

                    UserEntity commentedUser = new UserEntity();
                    commentedUser.setUsername(commentedUserUsername);
                    commentedUser.setEmail(commentedUsetEmail);
                    commentedUser.setImageUri(commentedUserImageURI);

                    Comment comment = new Comment();
                    comment.setCommentId(commentId);
                    comment.setCommentText(commentText);
                    comment.setCommentDate(commentDate);

                    postComments.add(comment);
                }

                Post post = new Post();
                post.setPostId(postId);
                post.setPostText(postText);
                post.setFileURL(postFileURI);
                post.setPostedDate(postedDate);
                post.setPostOwner(postOwner);
                post.setPostLocation(postLocation);
                post.setPostGroup(postGroup);
                post.setPostComments(postComments);
                post.setPostLikes(postLikes);

                posts.add(post);
            }
        } catch (JSONException e) {
            Log.e(TAG, "getPostsFromJSON: JSONException: " + e.getMessage(), e);
        } catch (ParseException e) {
            Log.e(TAG, "getPostsFromJSON: ParseException: " + e.getMessage(), e);
        }
        return posts;
    }
}
