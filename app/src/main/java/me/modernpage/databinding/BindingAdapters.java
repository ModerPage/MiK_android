package me.modernpage.databinding;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import me.modernpage.activity.R;
import me.modernpage.util.App;

/**
 * Data Binding adapters specific to the app.
 */
public class BindingAdapters {
    private static final String TAG = "BindingAdapters";

    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter(value = {"setFollowed"})
    public static void buttonState(Button button, boolean followed) {
        if (followed) {
            button.setText("UNFOLLOW");
        } else {
            button.setText("FOLLOW");
        }
    }

    @BindingAdapter(value = {"setMembersCount"})
    public static void membersCount(TextView textView, int count) {
        textView.setText(getMemberCountText(count));
    }

    private static String getMemberCountText(int count) {
        if (count == 0) {
            return null;
        } else if (count == 1) {
            return App.getResource().getString(R.string.group_counter_singular, count);
        } else if (count < 1000) {
            return App.getResource().getString(R.string.group_counter_plural, count);
        } else {
            return App.getResource().getString(R.string.group_counter_kplural, (count / 1000));
        }
    }
}
