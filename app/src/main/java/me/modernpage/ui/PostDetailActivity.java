//package me.modernpage.ui;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.util.Log;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.WindowManager;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.FrameLayout;
//import android.widget.ImageButton;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.ProgressBar;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.core.os.HandlerCompat;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.RequestManager;
//import com.bumptech.glide.request.RequestOptions;
//import com.google.android.exoplayer2.DefaultRenderersFactory;
//import com.google.android.exoplayer2.MediaItem;
//import com.google.android.exoplayer2.Player;
//import com.google.android.exoplayer2.RenderersFactory;
//import com.google.android.exoplayer2.SimpleExoPlayer;
//import com.google.android.exoplayer2.source.MediaSource;
//import com.google.android.exoplayer2.source.ProgressiveMediaSource;
//import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
//import com.google.android.exoplayer2.ui.PlayerView;
//import com.google.android.exoplayer2.upstream.DataSource;
//import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
//import com.google.android.exoplayer2.util.EventLogger;
//import com.google.android.exoplayer2.util.Util;
//import com.google.android.material.bottomsheet.BottomSheetDialog;
//import com.skyhope.showmoretextview.ShowMoreTextView;
//
//import java.text.DateFormat;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Locale;
//import java.util.concurrent.LinkedBlockingQueue;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//import me.modernpage.activity.R;
//import me.modernpage.entity.Comment;
//import me.modernpage.entity.Like;
//import me.modernpage.entity.Post;
//import me.modernpage.entity.UserEntity;
//import me.modernpage.fragment.home.OnMoreListener;
//import me.modernpage.task.DeletePost;
//import me.modernpage.task.GetPostById;
//import me.modernpage.task.ProcessPostComment;
//import me.modernpage.task.ProcessPostLike;
//import me.modernpage.util.App;
//import me.modernpage.util.Constants;
//
//import static android.view.View.GONE;
//import static android.view.View.VISIBLE;
//
//public class PostDetailActivity extends AppCompatActivity implements
//        GetPostById.OnGetPostByIdListener, ProcessPostLike.OnProcessPostLikeListener,
//        ProcessPostComment.OnProcessPostCommentListener {
//    private static final String TAG = "PostDetailActivity";
//
//    // ui
//    private CircleImageView mPostOwnerAvatar;
//    private TextView mPostOwnerUsername;
//    private TextView mPostDate;
//    private ImageButton mPostMore;
//    private ShowMoreTextView mPostDescription;
//    private FrameLayout mPostFileContainer;
//    private ProgressBar mPostVideoProgressbar;
//    private LinearLayout mPostLike;
//    private LinearLayout mPostShare;
//    private TextView mLikeCount;
//    private LinearLayout mPostCommentContainer;
//    private CircleImageView mCurrentUserAvatar;
//    private EditText mPostCommentText;
//    private Button mPostCommentSend;
//    private ImageView mPostThumbnail;
//    private ImageView mPostVolumeControl;
//    private ImageView mPostPlayControl;
//    private ProgressBar mPostProgressbar;
//    private BottomSheetDialog mSheetDialog;
//
//    // vars
//    private long postId;
//    private UserEntity mCurrentUser;
//    private RequestManager mRequestManager;
//
//
//    private enum FileType {IMAGE, VIDEO}
//
//    private enum PlayState {PLAY, PAUSE}
//
//    private enum VolumeState {ON, OFF}
//
//    private enum LikeState {LIKED, UNLIKED}
//
//    private FileType mPostFileType;
//    private PlayState mPlayState;
//    private VolumeState mVolumeState;
//    private LikeState mLikeState;
//    private PlayerView mVideoSurfaceView;
//    private SimpleExoPlayer mVideoPlayer;
//    private Like mLike;
//    private Post mCurrentPost;
//    private boolean isVideoAdded = false;
//    private ThreadPoolExecutor mExecutorService = new ThreadPoolExecutor(
//            1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(1));
//    private Handler mMainThreadHandler = HandlerCompat.createAsync(Looper.getMainLooper());
//    private ProcessPostLike mProcessPostLike;
//    private ProcessPostComment mProcessPostComment;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_post_detail);
//
//        init();
//
//        Bundle bundle = getIntent().getExtras();
//
//        if (bundle != null) {
//            postId = bundle.getLong(Post.class.getSimpleName(), -1L);
//            mCurrentUser = (UserEntity) bundle.getSerializable(UserEntity.class.getSimpleName());
//
//            if (postId != -1L) {
//                mPostProgressbar.setVisibility(VISIBLE);
//                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                GetPostById getPost = new GetPostById(this);
//                getPost.execute(postId);
//            }
//
//            if (mCurrentUser != null) {
//                mRequestManager.load(Constants.Network.BASE_URL + mCurrentUser.getImageUri())
//                        .into(mCurrentUserAvatar);
//            }
//        }
//    }
//
//    private void init() {
//        mPostOwnerAvatar = findViewById(R.id.post_detail_avatar);
//        mPostOwnerUsername = findViewById(R.id.post_detail_username);
//        mPostMore = findViewById(R.id.post_detail_more);
//        mPostDate = findViewById(R.id.post_detail_time);
//        mPostDescription = findViewById(R.id.post_detail_description);
//        mPostFileContainer = findViewById(R.id.post_detail_filecontainer);
//        mPostVideoProgressbar = findViewById(R.id.post_detail_video_progressbar);
//        mPostLike = findViewById(R.id.post_detail_like);
//        mPostShare = findViewById(R.id.post_detail_share);
//        mLikeCount = findViewById(R.id.post_detail_like_count);
//        mPostCommentContainer = findViewById(R.id.post_detail_comment_container);
//        mCurrentUserAvatar = findViewById(R.id.post_detail_comment_owner_avatar);
//        mPostCommentText = findViewById(R.id.post_detail_comment_edittext);
//        mPostCommentSend = findViewById(R.id.post_detail_send);
//        mPostThumbnail = findViewById(R.id.post_detail_thumbnail);
//        mPostVolumeControl = findViewById(R.id.post_detail_volume_control);
//        mPostPlayControl = findViewById(R.id.post_detail_play_control);
//        mPostProgressbar = findViewById(R.id.post_detail_progressbar);
//        mSheetDialog = new BottomSheetDialog(PostDetailActivity.this);
//
//        mRequestManager = initGlide();
//        mVideoSurfaceView = new PlayerView(this);
//        mVideoSurfaceView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
//        RenderersFactory renderersFactory = new DefaultRenderersFactory(this)
//                .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF);
//        mVideoPlayer = new SimpleExoPlayer.Builder(this, renderersFactory).build();
//        mVideoSurfaceView.setUseController(false);
//        mVideoSurfaceView.setPlayer(mVideoPlayer);
//        mVideoPlayer.addListener(mVideoPlayerListener);
//        mPlayState = PlayState.PLAY;
//        setVolumeControl(VolumeState.ON);
//
//        mProcessPostLike = new ProcessPostLike(this, mExecutorService, mMainThreadHandler);
//        mProcessPostComment = new ProcessPostComment(this, mExecutorService, mMainThreadHandler);
//    }
//
//    private Player.EventListener mVideoPlayerListener = new Player.EventListener() {
//        @Override
//        public void onPlaybackStateChanged(int state) {
//            switch (state) {
//                case Player.STATE_BUFFERING:
//                    Log.e(TAG, "onPlayerStateChanged: Buffering video.");
//                    if (mPostVideoProgressbar != null) {
//                        mPostVideoProgressbar.setVisibility(VISIBLE);
//                    }
//                    break;
//                case Player.STATE_ENDED:
//                    Log.d(TAG, "onPlayerStateChanged: Video ended.");
//                    mVideoPlayer.seekTo(0);
//                    break;
//                case Player.STATE_IDLE:
//
//                    break;
//                case Player.STATE_READY:
//                    Log.e(TAG, "onPlayerStateChanged: Ready to play.");
//                    if (mPostVideoProgressbar != null) {
//                        mPostVideoProgressbar.setVisibility(GONE);
//                    }
//                    if (!isVideoAdded) {
//                        addVideoView();
//                        if (mPlayState == PlayState.PAUSE)
//                            mVideoPlayer.pause();
//                    }
//                    break;
//                default:
//                    break;
//            }
//        }
//    };
//
//    private RequestManager initGlide() {
//        RequestOptions options = new RequestOptions()
//                .placeholder(R.drawable.placeholder)
//                .error(R.drawable.placeholder);
//
//        return Glide.with(this)
//                .setDefaultRequestOptions(options);
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mVideoPlayer != null) {
//            mVideoPlayer.release();
//            mVideoPlayer = null;
//        }
//    }
//
//    @Override
//    public void onGetPostByIdComplete(Post post) {
//        if (post == null) {
//            Toast.makeText(this, "Unable to load post detail", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        mCurrentPost = post;
//
//        mRequestManager.load(Constants.Network.BASE_URL + post.getPostOwner().getImageUri())
//                .into(mPostOwnerAvatar);
//
//        if (!"Public".equals(post.getPostGroup().getGroupName()) && !"Personal Feed".equals(post.getPostGroup().getGroupName()))
//            mPostOwnerUsername.setText(post.getPostOwner().getUsername() + " \u2023 " + post.getPostGroup().getGroupName());
//        else
//            mPostOwnerUsername.setText(post.getPostOwner().getUsername());
//
//        mPostDate.setText(setDate(post.getPostedDate()));
//
//
//        mPostDescription.setText(post.getPostText());
//
//        mPostFileType = getFileType(post);
//
//        mRequestManager.load(Constants.Network.BASE_URL + post.getFileURL())
//                .into(mPostThumbnail);
//        if (mPostFileType == FileType.VIDEO) {
//            mPostFileContainer.setOnClickListener(mVideoViewOnClickListener);
//            mPostVolumeControl.setOnClickListener(mVolumeControlOnClickListener);
//            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
//                    this, Util.getUserAgent(this, "Make It Known"));
//            String mediaUrl = post.getFileURL();
//            if (mediaUrl != null) {
//                MediaSource videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
//                        .createMediaSource(MediaItem.fromUri(Constants.Network.BASE_URL + mediaUrl));
//                mVideoPlayer.addAnalyticsListener(new EventLogger(null));
//                mVideoPlayer.setMediaSource(videoSource);
//                mVideoPlayer.prepare();
//                mVideoPlayer.play();
//            }
//        }
//
//
//        mLikeCount.setText(setPostLikeText(post.getPostLikes().size()));
//        mLike = currentUserLike(post);
//        if (mLike != null) {
//            setLikeControl(LikeState.LIKED);
//        } else {
//            setLikeControl(LikeState.UNLIKED);
//        }
//
//        mPostLike.setOnClickListener(mLikeOnClickListener);
//
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//        mPostProgressbar.setVisibility(GONE);
//        mPostCommentSend.setOnClickListener(mAddCommentClickListener);
//
//        for (Comment comment : mCurrentPost.getPostComments()) {
//            addCommentView(comment);
//        }
//
//        mPostShare.setOnClickListener(mShareOnClickListener);
//
//        mPostMore.setOnClickListener(mMoreOnClickListener);
//    }
//
//    private View.OnClickListener mMoreOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            if(mCurrentPost != null && mCurrentUser != null) {
//                View dialogView = getLayoutInflater().inflate(R.layout.layout_post_bottom_sheet, null);
////                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(PostDetailActivity.this);
//                boolean myPost = mCurrentPost.getPostOwner().getEmail().equals(mCurrentUser.getEmail());
//                if(myPost) {
//
//                    // delete post, edit post
//                    ConstraintLayout deletePost = dialogView.findViewById(R.id.post_more_delete_post);
//                    deletePost.setVisibility(VISIBLE);
//                    deletePost.setOnClickListener(mMoreItemClickListener);
//
//                    ConstraintLayout editPost = dialogView.findViewById(R.id.post_more_edit_post);
//                    editPost.setVisibility(VISIBLE);
//                    editPost.setOnClickListener(mMoreItemClickListener);
//
//                } else {
//
//                    // find support or report post, hide post
//
//                    ConstraintLayout reportPost = dialogView.findViewById(R.id.post_more_report_post);
//                    reportPost.setVisibility(VISIBLE);
//
//                    ConstraintLayout hidePost = dialogView.findViewById(R.id.post_more_hide_post);
//                    hidePost.setVisibility(VISIBLE);
//                    hidePost.setOnClickListener(mMoreItemClickListener);
//                }
//                mSheetDialog.setContentView(dialogView);
//                mSheetDialog.setCanceledOnTouchOutside(true);
//                mSheetDialog.show();
//            }
//        }
//    };
//
//    private View.OnClickListener mShareOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            if (mCurrentPost != null) {
//                Intent sendIntent = new Intent();
//                sendIntent.setAction(Intent.ACTION_SEND);
//                String shareData = mCurrentPost.getPostText() + " by " + mCurrentPost.getPostOwner().getUsername() + ". "
//                        + Constants.Network.BASE_URL + mCurrentPost.getFileURL() + " On MiK app";
//                sendIntent.putExtra(Intent.EXTRA_TEXT, shareData);
//                sendIntent.setType("text/*");
//
//
//                Intent shareIntent = Intent.createChooser(sendIntent, null);
//                startActivity(shareIntent);
////                }
//            }
//        }
//    };
//
//    private View.OnClickListener mMoreItemClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//
//            if(mSheetDialog.isShowing())
//                mSheetDialog.dismiss();
//
//            switch (view.getId()) {
//                case R.id.post_more_delete_post: {
//                    DeletePost deletePost = new DeletePost(new DeletePost.OnDeletePostListener() {
//                        @Override
//                        public void onDeletePostSuccess(long postId) {
//                            mPostProgressbar.setVisibility(GONE);
//                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                            Intent data = new Intent();
//                            data.putExtra(Post.class.getSimpleName(), postId);
//                            data.putExtra(OnMoreListener.class.getSimpleName(),1);
//                            setResult(RESULT_OK, data);
//                            finish();
//                        }
//
//                        @Override
//                        public void onDeletePostFailure(String error) {
//                            mPostProgressbar.setVisibility(GONE);
//                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                            Toast.makeText(PostDetailActivity.this, error, Toast.LENGTH_SHORT).show();
//                        }
//                    });
//                    deletePost.execute(mCurrentPost);
//                    break;
//                }
//                case R.id.post_more_edit_post: {
//                    Intent data = new Intent();
//                    data.putExtra(Post.class.getSimpleName(), mCurrentPost.getPostId());
//                    data.putExtra(OnMoreListener.class.getSimpleName(), 2);
//                    setResult(RESULT_OK, data);
//                    finish();
//                    break;
//                }
//                case R.id.post_more_hide_post: {
//                    mPostProgressbar.setVisibility(VISIBLE);
//                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
//                    Intent data = new Intent();
//                    data.putExtra(Post.class.getSimpleName(), mCurrentPost.getPostId());
//                    data.putExtra(OnMoreListener.class.getSimpleName(), 3);
//                    setResult(RESULT_OK, data);
//                    finish();
//                    break;
//                }
//                case R.id.post_more_report_post: {
//                    // add functions over here
//                    break;
//                }
//                default:
//                    throw new IllegalStateException("Can't do anything with this id: " + view.getId());
//            }
//        }
//    };
//
//    private View.OnClickListener mAddCommentClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            String commentText = mPostCommentText.getText().toString();
//            if (commentText.trim().length() > 0) {
//                if (mCurrentPost != null && mCurrentUser != null) {
//                    Comment comment = new Comment();
//                    comment.setCommentText(commentText);
//                    comment.setCommentedPost(mCurrentPost);
//                    comment.setCommentOwner(mCurrentUser);
//                    mProcessPostComment.addComment(comment);
//                }
//            }
//        }
//    };
//
//    private View.OnClickListener mLikeOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            if (mLikeState == LikeState.LIKED) {
//                Log.d(TAG, "onClick: deleteLike starts");
//                setLikeControl(LikeState.UNLIKED);
//                Like removingLike = mLike;
//
//                if (removingLike != null) {
//                    mProcessPostLike.deleteLike(removingLike);
//                }
//            } else {
//                Log.d(TAG, "onClick: addLike starts");
//
//                setLikeControl(LikeState.LIKED);
//
//                if (mLike == null) {
//                    Like newLike = new Like();
//                    newLike.setLikeOwner(mCurrentUser);
//                    newLike.setLikedPost(mCurrentPost);
//                    mProcessPostLike.addLike(newLike);
//                }
//            }
//        }
//    };
//
//    private void addVideoView() {
//        isVideoAdded = true;
//        mPostFileContainer.addView(mVideoSurfaceView);
//        mVideoSurfaceView.requestFocus();
//        mPostThumbnail.setVisibility(GONE);
//        mPostVolumeControl.bringToFront();
//        mPostVolumeControl.setVisibility(VISIBLE);
//
//    }
//
//    private View.OnClickListener mVideoViewOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            playPause();
//        }
//    };
//
//    private View.OnClickListener mVolumeControlOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            toggleVolume();
//        }
//    };
//
//    private void playPause() {
//        if (mVideoPlayer != null) {
//            if (mPlayState == PlayState.PAUSE) {
//                Log.d(TAG, "playPause: playing video");
//                setPlayControl(PlayState.PLAY);
//
//            } else if (mPlayState == PlayState.PLAY) {
//                Log.d(TAG, "playPause: pausing video");
//                setPlayControl(PlayState.PAUSE);
//            }
//        }
//    }
//
//    private void setPlayControl(PlayState state) {
//        mPlayState = state;
//        if (state == PlayState.PAUSE) {
//            mVideoPlayer.pause();
//            animatePlayControl();
//        } else if (state == PlayState.PLAY) {
//            mVideoPlayer.setPlayWhenReady(true);
//            animatePlayControl();
//        }
//    }
//
//    private void animatePlayControl() {
//        if (mPostPlayControl != null) {
//            mPostPlayControl.bringToFront();
//
//            if (mPlayState == PlayState.PAUSE)
//                mPostPlayControl.setImageResource(R.drawable.ic_pause_24dp);
//
//            else if (mPlayState == PlayState.PLAY)
//                mPostPlayControl.setImageResource(R.drawable.ic_play_arrow_24dp);
//
//            mPostPlayControl.animate().cancel();
//            mPostPlayControl.setAlpha(1f);
//
//            mPostPlayControl.animate()
//                    .alpha(0f)
//                    .setDuration(600)
//                    .setStartDelay(1000);
//        }
//    }
//
//    private void toggleVolume() {
//        if (mVideoPlayer != null) {
//            if (mVolumeState == VolumeState.OFF) {
//                Log.d(TAG, "togglePlaybackState: enabling volume.");
//                setVolumeControl(VolumeState.ON);
//
//            } else if (mVolumeState == VolumeState.ON) {
//                Log.d(TAG, "togglePlaybackState: disabling volume.");
//                setVolumeControl(VolumeState.OFF);
//
//            }
//        }
//    }
//
//    private void setVolumeControl(VolumeState state) {
//        Log.d(TAG, "setVolumeControl: state: " + state);
//        mVolumeState = state;
//        if (state == VolumeState.OFF)
//            mVideoPlayer.setVolume(0f);
//        else if (state == VolumeState.ON)
//            mVideoPlayer.setVolume(1f);
//
//        showVolumeControl();
//    }
//
//    private void showVolumeControl() {
//        if (mPostVolumeControl != null) {
//            mPostVolumeControl.bringToFront();
//            if (mVolumeState == VolumeState.OFF)
//                mPostVolumeControl.setImageResource(R.drawable.ic_volume_off_grey_24dp);
//            else if (mVolumeState == VolumeState.ON)
//                mPostVolumeControl.setImageResource(R.drawable.ic_volume_up_grey_24dp);
//
//        }
//    }
//
//    private FileType getFileType(Post post) {
//        String extension = post.getFileURL().substring(post.getFileURL().lastIndexOf(".") + 1);
//
//        if (extension.equalsIgnoreCase("jpg") ||
//                extension.equalsIgnoreCase("jpeg") ||
//                extension.equalsIgnoreCase("png")) {
//            return FileType.IMAGE;
//        } else if (extension.equalsIgnoreCase("mp4") ||
//                extension.equalsIgnoreCase("mov") ||
//                extension.equalsIgnoreCase("AVI") ||
//                extension.equalsIgnoreCase("wmv") ||
//                extension.equalsIgnoreCase("flv") ||
//                extension.equalsIgnoreCase("webm")) {
//            return FileType.VIDEO;
//        }
//        return null;
//    }
//
//    private String setDate(Date postDate) {
//        Date now = new Date();
//        long diffInMillies = now.getTime() - postDate.getTime();
//        int diffInMinutes = (int) (diffInMillies / (1000 * 60));
//        int diffInHours = diffInMinutes / 60;
//        int diffInDays = diffInHours / 24;
//
//        if (diffInMinutes >= 0 && diffInMinutes < 60) {
//            String minuteText = diffInMinutes < 1 ? "just now" : diffInMinutes + " minutes ago";
//            return minuteText;
//        } else if (diffInHours > 0 && diffInHours < 24) {
//            String hourText = diffInHours == 1 ? diffInHours + " hour ago" : diffInHours + " hours ago";
//            return hourText;
//        } else if (diffInDays > 0 && diffInDays < 6) {
//            String dayText = diffInDays == 1 ? diffInDays + " day ago" : diffInDays + " days ago";
//            return dayText;
//        } else {
//            DateFormat format = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm", Locale.getDefault());
//            return format.format(postDate);
//        }
//    }
//
//    private String setPostLikeText(long likesCount) {
//        String likeCountText;
//        if (likesCount <= 1) {
//            likeCountText = App.getResource().getString(R.string.like_counter_singular);
//        } else if (likesCount < 1000) {
//            likeCountText = App.getResource().getString(R.string.like_counter_plural);
//        } else {
//            likeCountText = App.getResource().getString(R.string.like_counter_kplural);
//        }
//
//        return (likesCount < 1000 ? likesCount : likesCount / 1000) + likeCountText;
//    }
//
//    private Like currentUserLike(Post post) {
//        for (Like like : post.getPostLikes()) {
//            if (mCurrentUser.getEmail().equals(like.getLikeOwner().getEmail()))
//                return like;
//        }
//        return null;
//    }
//
//    private void setLikeControl(LikeState state) {
//        mLikeState = state;
//        ImageView likeImage = mPostLike.findViewById(R.id.post_detail_like_image);
//        if (state == LikeState.LIKED) {
//            likeImage.setImageResource(R.drawable.ic_like_filled);
//        } else if (state == LikeState.UNLIKED) {
//            likeImage.setImageResource(R.drawable.ic_like_unfilled);
//        }
//    }
//
//    @Override
//    public void onAddPostLikeComplete(Like addedLike) {
//        mLike = addedLike;
//        addedLike.setLikedPost(mCurrentPost);
//        addedLike.setLikeOwner(mCurrentUser);
//        mCurrentPost.getPostLikes().add(addedLike);
//        mLikeCount.setText(setPostLikeText(mCurrentPost.getPostLikes().size()));
//    }
//
//    @Override
//    public void onDeletePostLikeComplete(Like removedLike) {
//        if (removedLike == null)
//            return;
//
//        mCurrentPost.getPostLikes().remove(mLike);
//        mLikeCount.setText(setPostLikeText(mCurrentPost.getPostLikes().size()));
//        mLike = null;
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (mVideoPlayer != null && isVideoAdded) {
//            if (mPlayState == PlayState.PLAY)
//                mVideoPlayer.play();
//        }
//
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (mVideoPlayer != null)
//            mVideoPlayer.pause();
//    }
//
//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        outState.putSerializable(PlayState.class.getSimpleName(), mPlayState);
//        outState.putSerializable(VolumeState.class.getSimpleName(), mVolumeState);
//        super.onSaveInstanceState(outState);
//    }
//
//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        mPlayState = (PlayState) savedInstanceState.getSerializable(PlayState.class.getSimpleName());
//        mVolumeState = (VolumeState) savedInstanceState.getSerializable(VolumeState.class.getSimpleName());
//        setVolumeControl(mVolumeState);
//    }
//
//    @Override
//    public void onAddPostCommentComplete(Comment addedComment) {
//        if (addedComment == null) {
//            Toast.makeText(this, "adding comment failed", Toast.LENGTH_LONG).show();
//            return;
//        }
//        mPostCommentText.getText().clear();
//
//        addedComment.setCommentOwner(mCurrentUser);
//        addedComment.setCommentedPost(mCurrentPost);
//
////        mCurrentPost.getPostComments().add(addedComment);
//        addCommentView(addedComment);
//
//    }
//
//    private void addCommentView(Comment comment) {
//        View commentView = LayoutInflater.from(this).inflate(R.layout.layout_comment_view_item,
//                mPostCommentContainer, false);
//
//        ImageView commentAvatar = commentView.findViewById(R.id.comment_view_avatar);
//        TextView commentOwnerName = commentView.findViewById(R.id.comment_view_owner_fullname);
//        TextView commentText = commentView.findViewById(R.id.comment_view_text);
//        TextView commentTime = commentView.findViewById(R.id.comment_view_time);
//        TextView commentDelete = commentView.findViewById(R.id.comment_view_delete);
//        mRequestManager.load(Constants.Network.BASE_URL + comment.getCommentOwner().getImageUri())
//                .into(commentAvatar);
//        commentOwnerName.setText(comment.getCommentOwner().getFullname());
//        commentText.setText(comment.getCommentText());
//        commentTime.setText(setDate(comment.getCommentDate()));
//
//        commentDelete.setClickable(true);
//        commentDelete.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "onClick: comment delete clicked");
//                mPostCommentContainer.removeView(commentView);
//                mProcessPostComment.deleteComment(comment);
//            }
//        });
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        params.gravity = Gravity.TOP | Gravity.START;
//
//        mPostCommentContainer.addView(commentView, params);
//    }
//
//    @Override
//    public void onDeletePostCommentComplete(Comment removedComment) {
//        Log.d(TAG, "onDeletePostCommentComplete: removedComment: " + removedComment.getCommentText());
//    }
//}
