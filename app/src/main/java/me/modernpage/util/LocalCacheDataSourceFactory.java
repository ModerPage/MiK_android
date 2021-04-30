package me.modernpage.util;

import android.app.Application;

import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import me.modernpage.activity.R;

public class LocalCacheDataSourceFactory implements DataSource.Factory {
    private final DefaultDataSourceFactory mDefaultDataSourceFactory;
    private final SimpleCache mSimpleCache;
    private final long maxFileSize = 100 * 1024 * 1024, maxCacheSize = 10 * 1024 * 1024;
    private final CacheDataSink mCacheDataSink;
    private final FileDataSource mFileDataSource;
    private static LocalCacheDataSourceFactory localCacheDataSourceFactory;
    private final DefaultHttpDataSourceFactory mDefaultHttpDataSourceFactory;

    private LocalCacheDataSourceFactory(Application context) {
        mSimpleCache = new SimpleCache(new File(context.getCacheDir(), "media"),
                new LeastRecentlyUsedCacheEvictor(maxCacheSize), new ExoDatabaseProvider(context));
        mCacheDataSink = new CacheDataSink(mSimpleCache, maxCacheSize);
        mFileDataSource = new FileDataSource();
        String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));
        mDefaultHttpDataSourceFactory = new DefaultHttpDataSourceFactory(userAgent);
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();
        mDefaultDataSourceFactory = new DefaultDataSourceFactory(context, (TransferListener) bandwidthMeter, mDefaultHttpDataSourceFactory);
    }

    public static LocalCacheDataSourceFactory getInstance(Application context) {
        if (localCacheDataSourceFactory == null) {
            synchronized (LocalCacheDataSourceFactory.class) {
                if (localCacheDataSourceFactory == null) {
                    localCacheDataSourceFactory = new LocalCacheDataSourceFactory(context);
                }
            }
        }
        return localCacheDataSourceFactory;
    }

    @NotNull
    @Override
    public DataSource createDataSource() {
        return new CacheDataSource(mSimpleCache, mDefaultDataSourceFactory.createDataSource(),
                mFileDataSource, mCacheDataSink, CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null);
    }

    public void setToken(String token) {
        mDefaultHttpDataSourceFactory.getDefaultRequestProperties().set("Authorization", token);
    }
}
