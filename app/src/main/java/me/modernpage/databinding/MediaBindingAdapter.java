package me.modernpage.databinding;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.File;

import me.modernpage.activity.R;
import me.modernpage.interceptor.AuthInterceptor;
import me.modernpage.util.App;
import me.modernpage.util.LocalCacheDataSourceFactory;


public class MediaBindingAdapter {
    private static final String TAG = "ImageBindingAdapter";

    /*********************Image Binding****************************/

    RequestManager mRequestManager;
    AuthInterceptor mAuthInterceptor;

    public MediaBindingAdapter(RequestManager requestManager, AuthInterceptor authInterceptor) {
        mRequestManager = requestManager;
        mAuthInterceptor = authInterceptor;
    }

    @BindingAdapter(value = {"setImageByUri", "placeholder"})
    public void setImage(ImageView view, Uri imageURI, Drawable placeholder) {
        if (imageURI != null) {
            mRequestManager
                    .load(imageURI)
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(view);
        }
    }

    @BindingAdapter(value = {"setImageByUrl"})
    public void setImage(ImageView view, String imageUrl) {
        if (imageUrl != null && mAuthInterceptor.getToken() != null) {
            GlideUrl glideUrl = new GlideUrl(imageUrl,
                    new LazyHeaders.Builder().addHeader("Authorization", mAuthInterceptor.getToken()).build());
            mRequestManager.load(glideUrl).into(view);
        }
    }

    @BindingAdapter(value = {"setFile", "placeholder"})
    public void setFile(ImageView view, File file, Drawable placeholder) {
        if (file != null) {
            mRequestManager
                    .load(Uri.fromFile(file))
                    .placeholder(placeholder)
                    .error(placeholder)
                    .into(view);
        }
    }


    /**********************Video Binding****************************/

    public static ObservableField<Boolean> isMuted = new ObservableField<>(false);
    private static SimpleExoPlayer player;
    private static boolean isPlaying = true;

    public static void pause() {
        if (player != null && player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
        }
    }

    @BindingAdapter(value = {"videoUrl", "thumbnail", "progressBar", "volumeControl", "playControl"}, requireAll = false)
    public void setVideo(PlayerView playerView, String videoUrl, ImageView thumbnail,
                         ProgressBar progressBar, ImageView volumeControl, ImageView playControl) {
//        if(player == null) {
        player = new SimpleExoPlayer.Builder(playerView.getContext()).build();
        player.addListener(new Player.EventListener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_BUFFERING:
                        // Buffering..
                        // set progress bar visible here
                        // set thumbnail visible
                        thumbnail.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case Player.STATE_READY:
                        // [PlayerView] has fetched the video duration so this is
                        // the block to hide the buffering progress bar
                        progressBar.setVisibility(View.GONE);
                        thumbnail.setVisibility(View.GONE);
                        break;
                    case Player.STATE_ENDED:
                        player.seekTo(0);
                        break;
                    case Player.STATE_IDLE:
                        break;
                }
            }
        });
//        }

        volumeControl.setOnClickListener(view -> {
            if (isMuted.get()) {
                player.setVolume(1f);
                isMuted.set(false);
            } else {
                player.setVolume(0f);
                isMuted.set(true);
            }
        });


        LocalCacheDataSourceFactory instance = LocalCacheDataSourceFactory.getInstance(App.getInstance());
        instance.setToken(mAuthInterceptor.getToken());
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(
                instance)
                .createMediaSource(MediaItem.fromUri(videoUrl));
        player.setMediaSource(mediaSource);
        player.prepare();
        isPlaying = true;
        player.setPlayWhenReady(true);
        animatePlayControl(playControl, isPlaying);
        player.setVolume(isMuted.get() ? 0f : 1f);
        playerView.setPlayer(player);
        playControl.setOnClickListener(view -> {
            player.setPlayWhenReady(!isPlaying);
            animatePlayControl(playControl, player.getPlayWhenReady());
        });
    }

    public static void play() {
        if (player != null) {
            player.setPlayWhenReady(isPlaying);
        }
    }


    public static void release() {
        if (player != null) {
            player.release();
        }
    }

    private static void animatePlayControl(ImageView imageView, boolean state) {
        isPlaying = state;
        imageView.bringToFront();
        if (state) {
            imageView.setImageResource(R.drawable.ic_play_arrow_24dp);
            imageView.animate().cancel();
            imageView.setAlpha(1f);

            imageView.animate()
                    .alpha(0f)
                    .setDuration(600)
                    .setStartDelay(1000);
        } else {
            imageView.setImageResource(R.drawable.ic_pause_24dp);
            imageView.animate().cancel();
            imageView.setAlpha(1f);
        }
    }
}
