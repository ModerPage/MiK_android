package me.modernpage.fragment.post;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.modernpage.activity.R;

public class CustomVideoLayout extends FrameLayout {
    private VideoView mVideoView;
    private View mCloseButton;

    public CustomVideoLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CustomVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomVideoLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        if (isInEditMode())
            return;

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View customView = null;

        if (inflater != null)
            customView = inflater.inflate(R.layout.layout_custom_videoview, this);

        if (customView == null)
            return;

        mVideoView = (VideoView) customView.findViewById(R.id.custom_videoview);
        mVideoView.setClipToOutline(true);
        mCloseButton = customView.findViewById(R.id.custom_video_close_button);
    }

    public VideoView getVideoView() {
        return mVideoView;
    }

    public View getCloseButton() {
        return mCloseButton;
    }
}
