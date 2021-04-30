package me.modernpage.databinding;

import androidx.databinding.DataBindingComponent;

import com.bumptech.glide.RequestManager;

import javax.inject.Inject;

import me.modernpage.interceptor.AuthInterceptor;

public class CustomDataBindingComponent implements DataBindingComponent {
    private final MediaBindingAdapter mMediaBindingAdapter;

    @Inject
    public CustomDataBindingComponent(RequestManager requestManager, AuthInterceptor authInterceptor) {
        mMediaBindingAdapter = new MediaBindingAdapter(requestManager, authInterceptor);
    }

    @Override
    public MediaBindingAdapter getMediaBindingAdapter() {
        return mMediaBindingAdapter;
    }

}
