package me.modernpage.fragment.home;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.RequestManager;

import me.modernpage.activity.R;
import me.modernpage.entity.Post;
import me.modernpage.util.Constants;

public class HomeRecyclerViewHolderImage extends HomeRecyclerViewHolder {
    ImageView mPostImage;

    public HomeRecyclerViewHolderImage(@NonNull View itemView) {
        super(itemView);
        mPostImage = itemView.findViewById(R.id.userpost_image);
    }

    @Override
    public void onBind(Post post, RequestManager requestManager) {
        super.onBind(post, requestManager);
        requestManager.load(Constants.Network.BASE_URL + post.getFileURL())
                .into(mPostImage);
    }
}
