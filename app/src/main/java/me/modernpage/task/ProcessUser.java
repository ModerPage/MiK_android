package me.modernpage.task;

import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import me.modernpage.Constants;
import me.modernpage.entity.UserEntity;


public class ProcessUser extends AsyncTask<String,Void, UserEntity> {
    private static final String TAG = "PocessUser";

    public interface OnProcessUser{
        void onProcessUserFinished(UserEntity user);
    }

    private OnProcessUser mCallback;

    public ProcessUser(OnProcessUser callback) {
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected UserEntity doInBackground(String... strings) {
        Log.d(TAG, "doInBackground: starts");
        if(strings == null)
            return null;
        HttpURLConnection connection;
        try {
            URL url = new URL(Constants.Network.GETUSER_URL);
            connection =(HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setReadTimeout(10000);
            connection.setConnectTimeout(15000);
            connection.setRequestProperty("Content-type","stream");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.connect();

            OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream());
            osw.write(strings[0]);
            osw.close();

            ObjectInputStream ois = new ObjectInputStream(connection.getInputStream());
            UserEntity userEntity = (UserEntity) ois.readObject();
            ois.close();
            Log.d(TAG, "doInBackground: " + userEntity);
            return userEntity;
        } catch (IOException | ClassNotFoundException e) {
            Log.e(TAG, "doInBackground: " + e );
        }

        return null;
    }

    @Override
    protected void onPostExecute(UserEntity userEntity) {
        mCallback.onProcessUserFinished(userEntity);
    }

}
