package me.modernpage.fragment.home;

import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.RequestManager;

import java.util.concurrent.ThreadPoolExecutor;

import me.modernpage.activity.R;
import me.modernpage.entity.Post;
import me.modernpage.entity.UserEntity;
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
    public void onBind(UserEntity currentUser, Post post, RequestManager requestManager,
                       ThreadPoolExecutor executorService, Handler handler, OnPostClickListener listener) {
        super.onBind(currentUser, post, requestManager, executorService, handler, listener);
        requestManager.load(Constants.Network.BASE_URL + post.getFileURL())
                .into(mThumbnail);
    }
}
