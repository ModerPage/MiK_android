package me.modernpage.fragment.post;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.modernpage.activity.R;

public class CustomImageLayout extends FrameLayout {


    private ImageView mImageView;
    private View mCloseButton;

    public CustomImageLayout(@NonNull Context context) {
        super(context);
        init(context);
    }

    public CustomImageLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CustomImageLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(final Context context) {
        if (isInEditMode())
            return;

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View customView = null;

        if (inflater != null)
            customView = inflater.inflate(R.layout.layout_custom_imageview, this);

        if (customView == null)
            return;

        mImageView = (ImageView) customView.findViewById(R.id.custom_imageView);
        mImageView.setClipToOutline(true);
        mCloseButton = customView.findViewById(R.id.custom_image_close_button);
    }


    public ImageView getImageView() {
        return mImageView;
    }

    public View getCloseButton() {
        return mCloseButton;
    }
}
