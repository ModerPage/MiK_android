package me.modernpage.interceptor;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import me.modernpage.data.local.MiKDatabase;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class RefreshTokenInterceptor implements Interceptor {
    private static final String TAG = "RefreshTokenInterceptor";
    private final MiKDatabase mDatabase;
    private final AuthInterceptor mAuthInterceptor;

    public RefreshTokenInterceptor(MiKDatabase database, AuthInterceptor authInterceptor) {
        mDatabase = database;
        mAuthInterceptor = authInterceptor;
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Log.d(TAG, "intercept: called");
        Request request = chain.request();
        String currentToken = request.header("Authorization");
        Response response = chain.proceed(request);
        String refreshedToken = response.header("Authorization");
        Log.d(TAG, "intercept: currentToken: " + currentToken + ", refreshedToken: " + refreshedToken);
        if (currentToken != null && refreshedToken != null) {
            updateToken(currentToken, refreshedToken);
        }
        return response;
    }

    private void updateToken(String currentToken, String newToken) {
        Log.d(TAG, "updateToken: currentToken: " + currentToken + ", newToken: " + newToken);
        mDatabase.getUserDao().updateToken(currentToken, newToken);
        mAuthInterceptor.setToken(newToken);
    }
}
