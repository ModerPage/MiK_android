package me.modernpage.task;

import android.os.Handler;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ExecutorService;

import me.modernpage.entity.Comment;
import me.modernpage.util.Constants;

public class ProcessPostComment {
    private static final String TAG = "ProcessPostComment";
    private ExecutorService mExecutorService;
    private Handler mMainThreadHandler;
    private OnProcessPostCommentListener mCallback;

    public interface OnProcessPostCommentListener {
        void onAddPostCommentComplete(Comment addedComment);

        void onDeletePostCommentComplete(Comment removedComment);
    }

    public ProcessPostComment(OnProcessPostCommentListener callback,
                              ExecutorService executorService, Handler mainThreadHandler) {
        mExecutorService = executorService;
        mMainThreadHandler = mainThreadHandler;
        mCallback = callback;
    }

    public void deleteComment(Comment comment) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                Comment deletedComment = doInBackgroundDeleteComment(comment);
                notifyDeleteResult(deletedComment);
            }
        });
    }

    private void notifyDeleteResult(Comment deletedComment) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null) {
                    mCallback.onDeletePostCommentComplete(deletedComment);
                }
            }
        });
    }

    public void addComment(Comment comment) {
        mExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                Comment addedComment = doInBackgroundAddComment(comment);
                notifyAddResult(addedComment);
            }
        });
    }

    private void notifyAddResult(Comment addedComment) {
        mMainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCallback != null)
                    mCallback.onAddPostCommentComplete(addedComment);
            }
        });
    }

    private Comment doInBackgroundAddComment(Comment comment) {
        if (comment == null)
            return null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(Constants.Network.ADDCOMMENT_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-type", "application/json");
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(10000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();

            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(jsonRequestBody(comment).getBytes());
            outputStream.close();

            if (connection.getResponseCode() == 201) {
                StringBuilder result = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                for (String line = bufferedReader.readLine(); line != null; line = bufferedReader.readLine()) {
                    result.append(line).append("\n");
                }
                bufferedReader.close();
                return getCommentFromJSON(result.toString());
            }

        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackgroundAddComment: MalformedURLException: " + e.getMessage(), e);
        } catch (ProtocolException e) {
            Log.e(TAG, "doInBackgroundAddComment: ProtocolException: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, "doInBackgroundAddComment: IOException: " + e.getMessage(), e);
        } finally {
            if (connection != null)
                connection.disconnect();
        }

        return null;
    }

    private String jsonRequestBody(Comment comment) {
        JSONObject jsonComment = new JSONObject();
        try {
            jsonComment.put("commentText", comment.getCommentText());
            jsonComment.put("commentOwnerEmail", comment.getCommentOwner().getEmail());
            jsonComment.put("commentedPostId", comment.getCommentedPost().getPostId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonComment.toString();
    }

    private Comment getCommentFromJSON(String result) {
        try {
            JSONObject jsonComment = new JSONObject(result);
            long commentId = jsonComment.getLong("commentId");
            Date commentedDate = Constants.mDateFormat.parse((String) jsonComment.get("commentedDate"));
            String commentText = jsonComment.getString("commentText");
            Comment comment = new Comment();
            comment.setCommentId(commentId);
            comment.setCommentDate(commentedDate);
            comment.setCommentText(commentText);
            return comment;
        } catch (JSONException e) {
            Log.e(TAG, "getCommentFromJSON: JSONException: " + e.getMessage(), e);
        } catch (ParseException e) {
            Log.e(TAG, "getCommentFromJSON: ParseException: " + e.getMessage(), e);
        }
        return null;
    }

    private Comment doInBackgroundDeleteComment(Comment comment) {
        if (comment == null)
            return null;

        HttpURLConnection connection = null;
        try {
            URL url = new URL(Constants.Network.DELETECOMMENT_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Content-type", "application/json");
            connection.setRequestMethod("DELETE");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(10000);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();

            OutputStream os = connection.getOutputStream();
            os.write(deleteJsonRequestBody(comment).getBytes());
            os.close();

            InputStreamReader isr = new InputStreamReader(connection.getInputStream());
            char[] buffer = new char[128];
            StringBuilder result = new StringBuilder();
            int charsRead;
            while ((charsRead = isr.read(buffer)) != -1) {
                result.append(buffer, 0, charsRead);
            }
            isr.close();

            return getCommentFromJSON(result.toString());
        } catch (ProtocolException e) {
            Log.e(TAG, "doInBackgroundDeleteComment: ProtocolException: " + e.getMessage(), e);
        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackgroundDeleteComment: MalformedURLException: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, "doInBackgroundDeleteComment: IOException: " + e.getMessage(), e);
        }
        return null;
    }

    private String deleteJsonRequestBody(Comment comment) {
        JSONObject jsonLike = new JSONObject();
        try {
            Log.d(TAG, "jsonRequestBody: like: " + comment);
            jsonLike.put("commentId", comment.getCommentId());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonLike.toString();
    }

}
