package me.modernpage.task;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

enum DownloadStatus {IDLE,PROCESSING,NOT_INITIALIZED,FAILED_OR_EMPTY,OK}

public class LoadJSON {
    private static final String TAG = "LoadJSON";
    private DownloadStatus mDownloadStatus;
    private OnLoadJSON mCallback;

    interface OnLoadJSON {
        void onLoadJSONComplete(String data, DownloadStatus status);
    }

    public LoadJSON(OnLoadJSON callback) {
        mCallback = callback;
        mDownloadStatus = DownloadStatus.IDLE;
    }

    void runInSameThread(String URL) {
        if(mCallback != null) {
            mCallback.onLoadJSONComplete(downloadJSON(URL), mDownloadStatus);
        }
    }

    protected String downloadJSON(String URL) {
        BufferedReader reader = null;
        HttpURLConnection connection = null;
        if(URL == null) {
            mDownloadStatus = DownloadStatus.NOT_INITIALIZED;
            return null;
        }
        try {
            mDownloadStatus = DownloadStatus.PROCESSING;
            URL url = new URL(URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            int response = connection.getResponseCode();

            Log.d(TAG, "doInBackground: response code: " + response);
            StringBuilder result = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            for(String line=reader.readLine();line!=null;line = reader.readLine()) {
                result.append(line).append("\n");
            }
            Log.d(TAG, "downloadJSON: " + result.toString());
            mDownloadStatus = DownloadStatus.OK;
            return result.toString();
        } catch (MalformedURLException e) {
            Log.e(TAG, "doInBackground: Invalid URL " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: IOException reading data " + e.getMessage());
        } catch (SecurityException e) {
            Log.e(TAG, "doInBackground: Security Exception, Needs permission? " + e.getMessage());
        }

        mDownloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        return null;
    }
}
