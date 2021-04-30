package me.modernpage.interceptor;

import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private static final String TAG = "AuthInterceptor";
    private static final String NO_AUTH_HEADER_KEY = "No-Authentication";
    private String token;

    public AuthInterceptor() {
    }

    public void setToken(String token) {
        Log.d(TAG, "setToken: called: " + token);
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Log.d(TAG, "intercept: called");
        Request request = chain.request();

        Request.Builder requestBuilder = request.newBuilder();
        if (request.header(NO_AUTH_HEADER_KEY) == null) {
            if (token == null) {
                throw new RuntimeException("Token should be defined for auth apis");
            } else {
                requestBuilder.addHeader("Authorization", token);
            }
        }
        return chain.proceed(requestBuilder.build());
    }

}
