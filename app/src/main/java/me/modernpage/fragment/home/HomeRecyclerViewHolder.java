package me.modernpage.fragment.home;

import android.graphics.Color;
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
import java.util.Date;
import java.util.Locale;

import me.modernpage.activity.R;
import me.modernpage.entity.Post;
import me.modernpage.util.Constants;

public class HomeRecyclerViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "HomeRecyclerViewHolder";
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
    RequestManager mRequestManager;

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
    public void onBind(Post post, RequestManager requestManager) {
        mRequestManager = requestManager;
        parent.setTag(this);
        mRequestManager.load(Constants.Network.BASE_URL + post.getPostOwner().getImageUri())
                .into(mPostAvatar);

        if (!"Public".equals(post.getPostGroup().getGroupName()) && !"Personal Feed".equals(post.getPostGroup().getGroupName())) {
            mPostUsername.setText(post.getPostOwner().getUsername() + " \u2023 " + post.getPostGroup().getGroupName());
        } else {
            mPostUsername.setText(post.getPostOwner().getUsername());
        }

        // set post time on list item
        Date now = new Date();
        long diffInMillies = now.getTime() - post.getPostedDate().getTime();
        int diffInMinutes = (int) (diffInMillies / (1000 * 60));
        int diffInHours = diffInMinutes / 60;
        int diffInDays = diffInHours / 24;

        if (diffInMinutes >= 0 && diffInMinutes < 60) {
            String minuteText = diffInMinutes < 1 ? "just now" : diffInMinutes + " minutes ago";
            mPostTime.setText(minuteText);
        } else if (diffInHours > 0 && diffInHours < 24) {
            String hourText = diffInHours == 1 ? diffInHours + " hour ago" : diffInHours + " hours ago";
            mPostTime.setText(hourText);
        } else if (diffInDays > 0 && diffInDays < 6) {
            String dayText = diffInDays == 1 ? diffInDays + " day ago" : diffInDays + " days ago";
            mPostTime.setText(dayText);
        } else {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm", Locale.getDefault());
            mPostTime.setText(format.format(post.getPostedDate()));
        }


        // count lines of post description
        int lines = post.getPostText().split("\r\n|\r|\n").length;
        if (lines >= 4)
            mPostDescription.setShowingLine(2);
        mPostDescription.setText(post.getPostText());

        if (post.getPostLikes() != null) {
            String likeCountText = post.getPostLikes().size() <= 1 ? " Like" : " Likes";
            mLikeCount.setText(post.getPostLikes().size() + likeCountText);
        } else
            mLikeCount.setText("0 Like");

        if (post.getPostComments() != null) {
            String commentCountText = post.getPostComments().size() <= 1 ? " Comment" : " Comments";
            mCommentCount.setText(post.getPostComments().size() + commentCountText);
        } else
            mCommentCount.setText("0 Comment");
    }
}
