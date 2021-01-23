package me.modernpage.fragment.home;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

import me.modernpage.util.Constants;
import me.modernpage.activity.R;
import me.modernpage.entity.Post;

public class HomeRecyclerView extends RecyclerView {
    private static final String TAG = "HomeRecyclerView";

    private enum VolumeState {ON, OFF}

    private enum PlayState {PLAY, PAUSE}

    // ui
    private ImageView mThumbnail, mPlayControl, mVolumeControl;
    private ProgressBar mProgressBar;
    private View mViewHolderParent;
    private FrameLayout mFileContainer;
    private PlayerView mVideoSurfaceView;
    private SimpleExoPlayer mVideoPlayer;

    // vars
    private List<Post> mPosts = new ArrayList<>();
    private int mVideoSurfaceDefaulHeight = 0;
    private int mScreenDefaultHeight = 0;
    private Context mContext;
    private int mPlayPosition = -1;
    private boolean mIsVideoViewAdded;
    private RequestManager mRequestManager;

    private VolumeState mVolumeState;
    private PlayState mPlayState;

    public HomeRecyclerView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public HomeRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);

        mVideoSurfaceDefaulHeight = point.x;
        mScreenDefaultHeight = point.y;

        mVideoSurfaceView = new PlayerView(mContext);
        mVideoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);

        mVideoPlayer = new SimpleExoPlayer.Builder(context).build();
        mVideoSurfaceView.setUseController(false);
        mVideoSurfaceView.setPlayer(mVideoPlayer);
        setVolumeControl(VolumeState.ON);
        setPlayControl(PlayState.PLAY);

        addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Log.d(TAG, "onScrollStateChanged: called");
                    if (mThumbnail != null) {
                        mThumbnail.setVisibility(VISIBLE);
                    }

                    if (!recyclerView.canScrollVertically(1)) {
                        playVideo(true);
                    } else {
                        playVideo(false);
                    }
                }
            }
        });

        addOnChildAttachStateChangeListener(new OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {

            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {
                if (mViewHolderParent != null && mViewHolderParent.equals(view)) {
                    // dynamically remove imageView or video view based on file type
                    Log.d(TAG, "onChildViewDetachedFromWindow: called");
                    resetVideoView();
                    if (mVideoPlayer.isPlaying()) {
                        mVideoPlayer.pause();
                    }
                }
            }
        });

        mVideoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                switch (state) {
                    case Player.STATE_BUFFERING:
                        Log.e(TAG, "onPlayerStateChanged: Buffering video.");
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(VISIBLE);
                        }
                        break;
                    case Player.STATE_ENDED:
                        Log.d(TAG, "onPlayerStateChanged: Video ended.");
                        mVideoPlayer.seekTo(0);
                        break;
                    case Player.STATE_IDLE:

                        break;
                    case Player.STATE_READY:
                        Log.e(TAG, "onPlayerStateChanged: Ready to play.");
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(GONE);
                        }

                        if (!mIsVideoViewAdded) {
                            // adding video view to list item
                            // if (mFileType == HomeRecyclerViewHolder.FileType.VIDEO) {
                            addVideoView();
                            if (mPlayState == PlayState.PLAY)
                                mVideoPlayer.play();
                            //  }
                        }
                        break;
                    default:
                        break;
                }
            }
        });

    } // init

    private void playVideo(boolean isEndOfList) {
        int targetPosition;
        if (!isEndOfList) {
            int startPosition = ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
            int endPosition = ((LinearLayoutManager) getLayoutManager()).findLastVisibleItemPosition();

            if (endPosition - startPosition > 1)
                endPosition = startPosition + 1;


            if (startPosition < 0 || endPosition < 0)
                return;

            if (startPosition != endPosition) {
                int startPositionVideoHight = getVisibleVideoSurfaceHeight(startPosition);
                int endPositionVideoHeight = getVisibleVideoSurfaceHeight(endPosition);

                targetPosition = startPositionVideoHight > endPositionVideoHeight ? startPosition : endPosition;
            } else
                targetPosition = startPosition;

        } else
            targetPosition = mPosts.size() - 1;


        Log.d(TAG, "playVideo: target position: " + targetPosition);

        if (targetPosition == mPlayPosition)
            return;

        mPlayPosition = targetPosition;
        if (mVideoSurfaceView == null)
            return;

        mVideoSurfaceView.setVisibility(INVISIBLE);
        removeVideoView(mVideoSurfaceView);

        int currentPosition = targetPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();

        View child = getChildAt(currentPosition);
        if (child == null)
            return;

        HomeRecyclerViewHolder holder = (HomeRecyclerViewHolder) child.getTag();
        if (holder instanceof HomeRecyclerViewHolderVideo) {

            HomeRecyclerViewHolderVideo viewHolderVideo = (HomeRecyclerViewHolderVideo) holder;
            mThumbnail = viewHolderVideo.mThumbnail;
            mProgressBar = viewHolderVideo.mProgressBar;
            mVolumeControl = viewHolderVideo.mVolumeControl;
            mPlayControl = viewHolderVideo.mPlayControl;
            mFileContainer = viewHolderVideo.itemView.findViewById(R.id.userpost_filecontainer);
            mViewHolderParent = viewHolderVideo.itemView;
            mRequestManager = viewHolderVideo.mRequestManager;

            mVideoSurfaceView.setPlayer(mVideoPlayer);
            mFileContainer.setOnClickListener(mVideoViewOnClickListener);
            mVolumeControl.setOnClickListener(mVolumeControlOnClickListener);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                    mContext, Util.getUserAgent(mContext, "RecyclerView VideoPlayer"));
            String mediaUrl = mPosts.get(targetPosition).getFileURL();
            if (mediaUrl != null) {
                MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(MediaItem.fromUri(Constants.Network.BASE_URL + mediaUrl));
                mVideoPlayer.setMediaSource(videoSource);
                mVideoPlayer.prepare();
            }
        }

    }

    private int getVisibleVideoSurfaceHeight(int playPosition) {
        int at = playPosition - ((LinearLayoutManager) getLayoutManager()).findFirstVisibleItemPosition();
        View child = getChildAt(at);
        if (child == null)
            return 0;

        int[] location = new int[2];
        child.getLocationInWindow(location);

        if (location[1] < 0)
            return location[1] + mVideoSurfaceDefaulHeight;
        else
            return mScreenDefaultHeight - location[1];
    }

    private void addVideoView() {
        mFileContainer.addView(mVideoSurfaceView);
        mIsVideoViewAdded = true;
        mVideoSurfaceView.requestFocus();
        mVideoSurfaceView.setVisibility(VISIBLE);
        mVideoSurfaceView.setAlpha(1f);
        mThumbnail.setVisibility(GONE);
        mVolumeControl.bringToFront();
        mVolumeControl.setVisibility(VISIBLE);
    }

    private void resetVideoView() {
        if (mIsVideoViewAdded) {
            removeVideoView(mVideoSurfaceView);
            mPlayPosition = -1;
            mVideoSurfaceView.setVisibility(INVISIBLE);
            mVolumeControl.setVisibility(GONE);
            mThumbnail.setVisibility(VISIBLE);
        }
    }

    private void removeVideoView(PlayerView videoView) {
        ViewGroup parent = (ViewGroup) videoView.getParent();
        if (parent == null)
            return;
        int index = parent.indexOfChild(videoView);
        if (index >= 0) {
            parent.removeView(videoView);
            mIsVideoViewAdded = false;
            mViewHolderParent.setOnClickListener(null);
        }
    }

    private OnClickListener mVideoViewOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            playPause();
        }
    };

    private void playPause() {
        if (mVideoPlayer != null) {
            if (mPlayState == PlayState.PAUSE) {
                Log.d(TAG, "playPause: playing video");
                setPlayControl(PlayState.PLAY);

            } else if (mPlayState == PlayState.PLAY) {
                Log.d(TAG, "playPause: pausing video");
                setPlayControl(PlayState.PAUSE);
            }
        }
    }

    private void setPlayControl(PlayState state) {
        mPlayState = state;
        if (state == PlayState.PAUSE) {
            mVideoPlayer.pause();
            animatePlayControl();
        } else if (state == PlayState.PLAY) {
            mVideoPlayer.setPlayWhenReady(true);
            animatePlayControl();
        }
    }

    private void animatePlayControl() {
        if (mPlayControl != null) {
            mPlayControl.bringToFront();

            if (mPlayState == PlayState.PAUSE)
                mPlayControl.setImageResource(R.drawable.ic_pause_grey_24dp);

            else if (mPlayState == PlayState.PLAY)
                mPlayControl.setImageResource(R.drawable.ic_play_arrow_grey_24dp);

            mPlayControl.animate().cancel();
            mPlayControl.setAlpha(1f);

            mPlayControl.animate()
                    .alpha(0f)
                    .setDuration(600)
                    .setStartDelay(1000);
        }
    }

    private OnClickListener mVolumeControlOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            toggleVolume();
        }
    };

    private void toggleVolume() {
        if (mVideoPlayer != null) {
            if (mVolumeState == VolumeState.OFF) {
                Log.d(TAG, "togglePlaybackState: enabling volume.");
                setVolumeControl(VolumeState.ON);

            } else if (mVolumeState == VolumeState.ON) {
                Log.d(TAG, "togglePlaybackState: disabling volume.");
                setVolumeControl(VolumeState.OFF);

            }
        }
    }

    private void setVolumeControl(VolumeState state) {
        Log.d(TAG, "setVolumeControl: state: " + state);
        mVolumeState = state;
        if (state == VolumeState.OFF)
            mVideoPlayer.setVolume(0f);
        else if (state == VolumeState.ON)
            mVideoPlayer.setVolume(1f);

        showVolumeControl();
    }

    private void showVolumeControl() {
        if (mVolumeControl != null) {
            mVolumeControl.bringToFront();
            if (mVolumeState == VolumeState.OFF)
                mVolumeControl.setImageResource(R.drawable.ic_volume_off_grey_24dp);
            else if (mVolumeState == VolumeState.ON)
                mVolumeControl.setImageResource(R.drawable.ic_volume_up_grey_24dp);

            mVolumeControl.setVisibility(VISIBLE);
        }
    }

    public void releasePlayer() {
        if (mVideoPlayer != null) {
            mVideoPlayer.release();
            mVideoPlayer = null;
        }
        mViewHolderParent = null;
    }

    public List<Post> getPosts() {
        return mPosts;
    }

    public void stopPlaying() {
        if (mVideoPlayer != null && mIsVideoViewAdded) {
            if (mVideoPlayer.isPlaying()) {
                setPlayControl(PlayState.PAUSE);
            }
        }
    }
}
