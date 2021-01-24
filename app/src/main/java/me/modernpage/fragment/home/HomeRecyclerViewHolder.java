package me.modernpage.fragment.home;

import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.skyhope.showmoretextview.ShowMoreTextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import me.modernpage.activity.R;
import me.modernpage.entity.Comment;
import me.modernpage.entity.Like;
import me.modernpage.entity.Post;
import me.modernpage.entity.UserEntity;
import me.modernpage.util.App;
import me.modernpage.util.Constants;

public class HomeRecyclerViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "HomeRecyclerViewHolder";

    // ui
    View parent;
    ImageView mPostAvatar;
    TextView mPostUsername;
    TextView mPostTime;
    ImageButton mMore;
    ShowMoreTextView mPostDescription;
    FrameLayout mFileContainer;
    ProgressBar mProgressBar;
    LinearLayout mPostLike;
    LinearLayout mPostComment;
    LinearLayout mPostShare;
    TextView mLikeCount;
    TextView mCommentCount;

    // vars
    RequestManager mRequestManager;
    private boolean isLiked = false;

    public HomeRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        parent = itemView;
        mPostAvatar = itemView.findViewById(R.id.userpost_avatar);
        mPostUsername = itemView.findViewById(R.id.userpost_username);
        mPostTime = itemView.findViewById(R.id.userpost_time);
        mMore = itemView.findViewById(R.id.userpost_more);
        mPostDescription = itemView.findViewById(R.id.userpost_description);
        mPostDescription.addShowMoreText("Continue");
        mPostDescription.addShowLessText("Less");
        mPostDescription.setShowMoreColor(Color.RED);
        mPostDescription.setShowLessTextColor(Color.RED);

        mFileContainer = itemView.findViewById(R.id.userpost_filecontainer);
        mProgressBar = itemView.findViewById(R.id.userpost_progressbar);
        mPostLike = itemView.findViewById(R.id.userpost_like);
        mPostComment = itemView.findViewById(R.id.userpost_comment);
        mPostShare = itemView.findViewById(R.id.userpost_share);
        mLikeCount = itemView.findViewById(R.id.userpost_like_count);
        mCommentCount = itemView.findViewById(R.id.userpost_comment_count);
    }

    @CallSuper
    public void onBind(UserEntity currentUser, Post post, RequestManager requestManager) {

        mRequestManager = requestManager;
        parent.setTag(this);
        mRequestManager.load(Constants.Network.BASE_URL + post.getPostOwner().getImageUri())
                .into(mPostAvatar);

        if (!"Public".equals(post.getPostGroup().getGroupName()) && !"Personal Feed".equals(post.getPostGroup().getGroupName()))
            mPostUsername.setText(post.getPostOwner().getUsername() + " \u2023 " + post.getPostGroup().getGroupName());
        else
            mPostUsername.setText(post.getPostOwner().getUsername());


        // set post time on list item
        mPostTime.setText(setPostDate(post.getPostedDate()));

        // count lines of post description
        int lines = post.getPostText().split("\r\n|\r|\n").length;

        if (lines >= 4)
            mPostDescription.setShowingLine(2);

        mPostDescription.setText(post.getPostText());

        mLikeCount.setText(setPostLikeText(post.getPostLikes()));

        mCommentCount.setText(setPostCommentText(post.getPostComments()));

        mPostLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImageView likeImage = view.findViewById(R.id.userpost_like_image);
                Log.d(TAG, "onClick: isLiked: " + isLiked);
                if (isLiked) {
                    likeImage.setImageResource(R.drawable.ic_like_unfilled);
                    isLiked = false;
                } else {
                    likeImage.setImageResource(R.drawable.ic_like_filled);
                    isLiked = true;
                }
            }
        });
    }

    private String setPostDate(Date postDate) {
        Date now = new Date();
        long diffInMillies = now.getTime() - postDate.getTime();
        int diffInMinutes = (int) (diffInMillies / (1000 * 60));
        int diffInHours = diffInMinutes / 60;
        int diffInDays = diffInHours / 24;

        if (diffInMinutes >= 0 && diffInMinutes < 60) {
            String minuteText = diffInMinutes < 1 ? "just now" : diffInMinutes + " minutes ago";
            return minuteText;
        } else if (diffInHours > 0 && diffInHours < 24) {
            String hourText = diffInHours == 1 ? diffInHours + " hour ago" : diffInHours + " hours ago";
            return hourText;
        } else if (diffInDays > 0 && diffInDays < 6) {
            String dayText = diffInDays == 1 ? diffInDays + " day ago" : diffInDays + " days ago";
            return dayText;
        } else {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm", Locale.getDefault());
            return format.format(postDate);
        }
    }

    private String setPostCommentText(Collection<Comment> comments) {
        if (comments != null) {
            int commentCount = comments.size();
            String commentCountText;

            if (commentCount <= 1) {
                commentCountText = App.getResource().getString(R.string.comment_counter_suffix_singular);
            } else if (commentCount < 1000) {
                commentCountText = App.getResource().getString(R.string.comment_counter_suffix_plural);
            } else {
                commentCountText = App.getResource().getString(R.string.comment_counter_suffix_kplural);
            }

            return (commentCount < 1000 ? commentCount : (commentCount / 1000)) + commentCountText;
        }
        return null;
    }

    private String setPostLikeText(Collection<Like> likes) {
        if (likes != null) {
            int likeCount = likes.size();
            String likeCountText;

            if (likeCount <= 1) {

                likeCountText = App.getResource().getString(R.string.like_counter_suffix_singular);
            } else if (likeCount < 1000) {
                likeCountText = App.getResource().getString(R.string.like_counter_suffix_plural);
            } else {
                likeCountText = App.getResource().getString(R.string.like_counter_suffix_kplural);
            }

            return (likeCount < 1000 ? likeCount : likeCount / 1000) + likeCountText;
        }
        return null;
    }
}
