package me.modernpage.databinding;

import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.modernpage.activity.R;
import me.modernpage.util.App;

public class PostViewBindingAdapter {
    private static final String TAG = "PostViewBindingAdapter";


    @BindingAdapter("setDate")
    public static void setDate(TextView textView, Date date) {
        if (date == null)
            return;
        long diffInMillies = System.currentTimeMillis() - date.getTime();
        int diffInMinutes = (int) (diffInMillies / (1000 * 60));
        int diffInHours = diffInMinutes / 60;
        int diffInDays = diffInHours / 24;
        String dateText;
        if (diffInMinutes >= 0 && diffInMinutes < 60) {
            dateText = diffInMinutes < 1 ? "just now" : diffInMinutes + " minutes ago";
        } else if (diffInHours > 0 && diffInHours < 24) {
            dateText = diffInHours == 1 ? diffInHours + " hour ago" : diffInHours + " hours ago";
        } else if (diffInDays > 0 && diffInDays < 6) {
            dateText = diffInDays == 1 ? diffInDays + " day ago" : diffInDays + " days ago";
        } else {
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm", Locale.getDefault());
            dateText = format.format(date);
        }
        textView.setText(dateText);
    }

    @BindingAdapter("setLikesCount")
    public static void setLikesCount(TextView textView, long likesCount) {
        textView.setText(getLikesCountText(likesCount));
    }

    @BindingAdapter("setCommentsCount")
    public static void setCommentsCount(TextView textView, long commentsCount) {
        textView.setText(getCommentsCountText(commentsCount));
    }

    private static String getCommentsCountText(long commentsCount) {
        if (commentsCount == 0) {
            return null;
        } else if (commentsCount == 1) {
            return App.getResource().getString(R.string.comment_counter_singular, commentsCount);
        } else if (commentsCount < 1000) {
            return App.getResource().getString(R.string.comment_counter_plural, commentsCount);
        } else {
            return App.getResource().getString(R.string.comment_counter_kplural, (commentsCount / 1000));
        }
    }

    private static String getLikesCountText(long likesCount) {
        if (likesCount == 0) {
            return null;
        } else if (likesCount == 1) {
            return App.getResource().getString(R.string.like_counter_singular, likesCount);
        } else if (likesCount < 1000) {
            return App.getResource().getString(R.string.like_counter_plural, likesCount);
        } else {
            return App.getResource().getString(R.string.like_counter_kplural, (likesCount / 1000));
        }
    }
}
