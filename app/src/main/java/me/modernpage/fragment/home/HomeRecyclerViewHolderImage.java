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

public class HomeRecyclerViewHolderImage extends HomeRecyclerViewHolder {
    ImageView mPostImage;

    public HomeRecyclerViewHolderImage(@NonNull View itemView) {
        super(itemView);
        mPostImage = itemView.findViewById(R.id.userpost_image);
    }

    @Override
    public void onBind(UserEntity currentUser, Post post, RequestManager requestManager,
                       ThreadPoolExecutor executorService, Handler handler, OnPostClickListener listener) {
        super.onBind(currentUser, post, requestManager, executorService, handler, listener);
        requestManager.load(Constants.Network.BASE_URL + post.getFileURL())
                .into(mPostImage);
    }
}
