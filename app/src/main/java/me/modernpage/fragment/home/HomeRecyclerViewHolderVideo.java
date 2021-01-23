package me.modernpage.fragment.home;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.RequestManager;

import me.modernpage.activity.R;
import me.modernpage.entity.Post;
import me.modernpage.util.Constants;

public class HomeRecyclerViewHolderVideo extends HomeRecyclerViewHolder {
    ImageView mThumbnail, mVolumeControl, mPlayControl;

    public HomeRecyclerViewHolderVideo(@NonNull View itemView) {
        super(itemView);
        mThumbnail = itemView.findViewById(R.id.userpost_thumbnail);
        mVolumeControl = itemView.findViewById(R.id.userpost_volume_control);
        mPlayControl = itemView.findViewById(R.id.userpost_play_control);
    }

    @Override
    public void onBind(Post post, RequestManager requestManager) {
        super.onBind(post, requestManager);
        requestManager.load(Constants.Network.BASE_URL + post.getFileURL())
                .into(mThumbnail);
    }
}
