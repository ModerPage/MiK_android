package me.modernpage.fragment.home;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.skyhope.showmoretextview.ShowMoreTextView;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import me.modernpage.Constants;
import me.modernpage.activity.R;
import me.modernpage.entity.Post;

public class HomePostRecViewAdapter extends RecyclerView.Adapter<HomePostRecViewAdapter.PostViewHolder> {
    private static final String TAG = "HomePostRecViewAdapter";
    private List<Post> mPosts;
    private WeakReference<Context> mContext;

    public HomePostRecViewAdapter(Context context, List<Post> posts) {
        mPosts = posts;
        mContext = new WeakReference<>(context);
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_post, parent, false);
        return new PostViewHolder(view, mContext.get());
    }

    void loadNewPost(List<Post> posts) {
        mPosts = posts;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        if (mPosts != null && mPosts.size() > 0) {
            Post currentPost = mPosts.get(position);

            Picasso.get().load(Constants.Network.BASE_URL + mPosts.get(position).getPostOwner().getImageUri())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(holder.mPostAvatar);

            if (!"Public".equals(currentPost.getPostGroup().getGroupName()) && !"Personal Feed".equals(currentPost.getPostGroup().getGroupName())) {
                holder.mPostUsername.setText(currentPost.getPostOwner().getUsername() + " \u2023 " + currentPost.getPostGroup().getGroupName());
            } else {
                holder.mPostUsername.setText(currentPost.getPostOwner().getUsername());
            }

            Date now = new Date();
            long diffInMillies = now.getTime() - currentPost.getPostedDate().getTime();
            int diffInMinutes = (int) (diffInMillies / (1000 * 60));
            int diffInHours = diffInMinutes / 60;
            int diffInDays = diffInHours / 24;

            if (diffInDays > 0 && diffInDays < 6) {
                String dayText = diffInDays == 1 ? diffInDays + " day ago" : diffInDays + " days ago";
                holder.mPostTime.setText(dayText);
            } else if (diffInHours > 0) {
                String hourText = diffInHours == 1 ? diffInHours + " hour ago" : diffInHours + " hours ago";
                holder.mPostTime.setText(hourText);
            } else if (diffInMinutes >= 0) {
                String minuteText = diffInMinutes < 1 ? "just now" : diffInMinutes + " minutes ago";
                holder.mPostTime.setText(minuteText);
            } else {
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm", Locale.getDefault());
                holder.mPostTime.setText(format.format(currentPost.getPostedDate()));
            }

            // count lines of post description
            int lines = currentPost.getPostText().split("\r\n|\r|\n").length;
            Log.d(TAG, "onBindViewHolder: line number: " + lines);
            if (lines >= 4) {
                holder.mPostDescription.setShowingLine(2);
                holder.mPostDescription.setText(currentPost.getPostText());
            } else
                holder.mPostDescription.setText(currentPost.getPostText());

            String extension = currentPost.getFileURL().substring(currentPost.getFileURL().lastIndexOf(".") + 1);
            Log.d(TAG, "onBindViewHolder: fileType: " + extension);

            if (extension.equalsIgnoreCase("jpg") ||
                    extension.equalsIgnoreCase("jpeg") ||
                    extension.equalsIgnoreCase("png")) {
                Log.d(TAG, "onBindViewHolder: postImage fileURI: " + currentPost.getFileURL());
                holder.mPostVideo.setVisibility(View.GONE);
                holder.mPostImage.setVisibility(View.VISIBLE);
                Picasso.get().load(Constants.Network.BASE_URL + currentPost.getFileURL())
                        .error(R.drawable.placeholder)
                        .placeholder(R.drawable.placeholder)
                        .fit().centerCrop()
                        .into(holder.mPostImage);

            } else if (extension.equalsIgnoreCase("mp4") ||
                    extension.equalsIgnoreCase("mov") ||
                    extension.equalsIgnoreCase("AVI") ||
                    extension.equalsIgnoreCase("wmv") ||
                    extension.equalsIgnoreCase("flv") ||
                    extension.equalsIgnoreCase("webm")) {
                Log.d(TAG, "onBindViewHolder: postVideo uploading: " + extension);
                holder.mPostImage.setVisibility(View.GONE);
                holder.mPostVideo.setVisibility(View.VISIBLE);
                MediaItem mediaItem = MediaItem.fromUri(getVideoURI(Constants.Network.BASE_URL + currentPost.getFileURL()));
                holder.simpleExoPlayer.setMediaItem(mediaItem);
            }

            if (currentPost.getPostLikes() != null) {
                String likeCountText = currentPost.getPostLikes().size() <= 1 ? " Like" : " Likes";
                holder.mLikeCount.setText(currentPost.getPostLikes().size() + likeCountText);
            } else
                holder.mLikeCount.setText("0 Like");

            if (currentPost.getPostComments() != null) {
                String commentCountText = currentPost.getPostComments().size() <= 1 ? " Comment" : " Comments";
                holder.mCommentCount.setText(currentPost.getPostComments().size() + commentCountText);
            } else
                holder.mCommentCount.setText("0 Comment");

        }
    }

    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    private Uri getVideoURI(String fileURI) {
        if (URLUtil.isValidUrl(fileURI)) {
            return Uri.parse(fileURI);
        }
        return null;
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        ImageView mPostAvatar;
        TextView mPostUsername;
        TextView mPostTime;
        ShowMoreTextView mPostDescription;
        ImageView mPostImage;
        StyledPlayerView mPostVideo;
        SimpleExoPlayer simpleExoPlayer;
        LinearLayout mPostLike;
        LinearLayout mPostComment;
        LinearLayout mPostShare;
        TextView mLikeCount;
        TextView mCommentCount;
        ImageButton mMore;

        public PostViewHolder(@NonNull View view, Context context) {
            super(view);
            mPostAvatar = view.findViewById(R.id.userpost_avatar);
            mPostUsername = view.findViewById(R.id.userpost_username);
            mPostTime = view.findViewById(R.id.userpost_time);

            mPostDescription = view.findViewById(R.id.userpost_description);
            mPostDescription.addShowMoreText("Continue");
            mPostDescription.addShowLessText("Less");
            mPostDescription.setShowMoreColor(Color.RED);
            mPostDescription.setShowLessTextColor(Color.RED);

            mPostImage = view.findViewById(R.id.userpost_image);
            mPostVideo = view.findViewById(R.id.userpost_videoview);
            simpleExoPlayer = new SimpleExoPlayer.Builder(context).build();
            mPostVideo.setPlayer(simpleExoPlayer);
            simpleExoPlayer.setPlayWhenReady(true);

            mPostLike = view.findViewById(R.id.userpost_like);
            mPostLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            mPostComment = view.findViewById(R.id.userpost_comment);
            mPostComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            mPostShare = view.findViewById(R.id.userpost_share);
            mPostShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });

            mLikeCount = view.findViewById(R.id.userpost_like_count);
            mCommentCount = view.findViewById(R.id.userpost_comment_count);

            mMore = view.findViewById(R.id.userpost_more);
            mMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                }
            });
        }
    }
}
