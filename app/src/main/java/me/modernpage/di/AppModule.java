package me.modernpage.di;

import android.app.Application;

import androidx.room.Room;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tinder.scarlet.Lifecycle;
import com.tinder.scarlet.lifecycle.android.AndroidLifecycle;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import me.modernpage.activity.R;
import me.modernpage.data.local.MiKDatabase;
import me.modernpage.data.local.dao.GroupDao;
import me.modernpage.data.local.dao.PostDao;
import me.modernpage.data.local.dao.RateLimiterDao;
import me.modernpage.data.local.dao.UserDao;
import me.modernpage.data.remote.resource.GroupResource;
import me.modernpage.data.remote.resource.PostResource;
import me.modernpage.data.remote.resource.UserResource;
import me.modernpage.interceptor.AuthInterceptor;
import me.modernpage.interceptor.RefreshTokenInterceptor;
import me.modernpage.util.Constants;
import me.modernpage.util.ImagePopUpHelper;
import me.modernpage.util.LiveDataCallAdapterFactory;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    Retrofit provideRetrofit(Gson gson, OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(Constants.Network.ENDPOINT)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(new LiveDataCallAdapterFactory())
                .build();
    }

    @Provides
    @Singleton
    RequestManager provideGlide(Application application) {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.white_background)
                .error(R.drawable.white_background);
        return Glide.with(application)
                .setDefaultRequestOptions(options);
    }


    @Provides
    @Singleton
    OkHttpClient provideOkHttp(AuthInterceptor authInterceptor,
                               RefreshTokenInterceptor refreshTokenInterceptor) {
        return new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(refreshTokenInterceptor)
                .connectTimeout(10, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS).build();
    }


    @Provides
    @Singleton
    Lifecycle provideLifecycle(Application application) {
        return AndroidLifecycle.ofApplicationForeground(application);
    }

    @Provides
    @Singleton
    Gson provideGson() {
        return new GsonBuilder()
                .setDateFormat(new SimpleDateFormat(Constants.DATE_FORMAT_PATTERN,
                        Locale.getDefault()).toPattern())
                .create();
    }

    @Provides
    @Singleton
    AuthInterceptor provideAuthInterceptor() {
        return new AuthInterceptor();
    }

    @Provides
    @Singleton
    RefreshTokenInterceptor provideRefreshTokenInterceptor(AuthInterceptor authInterceptor,
                                                           MiKDatabase miKDatabase) {
        return new RefreshTokenInterceptor(miKDatabase, authInterceptor);
    }

    @Provides
    @Singleton
    ImagePopUpHelper provideImagePopUpHelper(AuthInterceptor authInterceptor, RequestManager requestManager) {
        return new ImagePopUpHelper(authInterceptor, requestManager);
    }

    @Provides
    @Singleton
    UserResource provideUserResource(Retrofit retrofit) {
        return retrofit.create(UserResource.class);
    }

    @Provides
    @Singleton
    GroupResource provideGroupResource(Retrofit retrofit) {
        return retrofit.create(GroupResource.class);
    }

    @Provides
    @Singleton
    PostResource providePostResource(Retrofit retrofit) {
        return retrofit.create(PostResource.class);
    }

    @Provides
    @Singleton
    MiKDatabase provideDatabase(Application application) {
        return Room.databaseBuilder(application, MiKDatabase.class, "mik.db").build();
    }

    @Provides
    @Singleton
    UserDao provideUserDao(MiKDatabase database) {
        return database.getUserDao();
    }

    @Provides
    @Singleton
    GroupDao provideGroupDao(MiKDatabase database) {
        return database.getGroupDao();
    }

    @Provides
    @Singleton
    PostDao providePostDao(MiKDatabase database) {
        return database.getPostDao();
    }

    @Provides
    @Singleton
    RateLimiterDao provideRateLimiterDao(MiKDatabase database) {
        return database.getRateLimiterDao();
    }

}
