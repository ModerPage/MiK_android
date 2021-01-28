package me.modernpage.task;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.modernpage.entity.Comment;
import me.modernpage.entity.Group;
import me.modernpage.entity.GroupType;
import me.modernpage.entity.Like;
import me.modernpage.entity.Location;
import me.modernpage.entity.Post;
import me.modernpage.entity.UserEntity;
import me.modernpage.util.Constants;

//TODO: exception handling, add status of processing
public class GetPostById extends AsyncTask<Long, Void, Post> {
    private static final String TAG = "GetPostById";
    private OnGetPostByIdListener mCallback;

    public interface OnGetPostByIdListener {
        void onGetPostByIdComplete(Post post);
    }

    public GetPostById(OnGetPostByIdListener callback) {
        mCallback = callback;
    }

    @Override
    protected Post doInBackground(Long... longs) {
        if (longs[0] == null)
            return null;
        HttpURLConnection connection = null;
        BufferedReader bufferedReader = null;
        try {
            URL url = new URL(Constants.Network.BASE_URL + createURI(longs[0]));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            connection.connect();

            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder result = new StringBuilder();
            for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                result.append(line).append("\n");
            }
            bufferedReader.close();

            return getPostFromJSON(result.toString());
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IOException: " + e.getMessage(), e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (connection != null)
                connection.disconnect();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Post post) {
        if (mCallback != null)
            mCallback.onGetPostByIdComplete(post);
    }

    private Post getPostFromJSON(String resultBody) {
        try {
            JSONObject postJSON = new JSONObject(resultBody);
            long postId = postJSON.getLong("postId");
            String postText = postJSON.getString("postText");
            String postFileURI = postJSON.getString("fileURI");
            String postedDateText = postJSON.getString("postedDate");
            Date postedDate = Constants.mDateFormat.parse(postedDateText);

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
                Date likedDate = Constants.mDateFormat.parse((String) likeJSON.get("likedDate"));
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
                Date commentDate = Constants.mDateFormat.parse((String) commentJSON.get("commentDate"));

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

            return post;
        } catch (JSONException e) {
            Log.e(TAG, "getPostFromJSON: JSONException: " + e.getMessage(), e);
        } catch (ParseException e) {
            Log.e(TAG, "getPostFromJSON: ParseException: " + e.getMessage(), e);
        }
        return null;
    }

    private String createURI(long postId) {
        return Uri.parse("/post/getPost").buildUpon()
                .appendQueryParameter("postId", String.valueOf(postId))
                .build()
                .toString();
    }
}
