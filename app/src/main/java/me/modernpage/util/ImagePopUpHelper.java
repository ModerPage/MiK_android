package me.modernpage.util;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import me.modernpage.interceptor.AuthInterceptor;

public class ImagePopUpHelper {
    private Activity context;

    private Drawable imageViewDrawable;
    private boolean requireResizingOfBitmap;

    private ImageView poppedImageView;
    private Dialog dialog;

    private final AuthInterceptor mAuthInterceptor;
    private final RequestManager mRequestManager;

    public ImagePopUpHelper(AuthInterceptor authInterceptor, RequestManager requestManager) {
        mAuthInterceptor = authInterceptor;
        mRequestManager = requestManager;
    }

    @SuppressWarnings("deprecation")
    private void cacheResizedDrawable(Drawable drawable, boolean shouldScaleDown, boolean shouldScaleUp) {
        imageViewDrawable = drawable;
        int imageRealWidth = imageViewDrawable.getIntrinsicWidth();
        int imageRealHeight = imageViewDrawable.getIntrinsicHeight();

        Point screenDimensions = getScreenDimensions(context);
        final int screenWidth = screenDimensions.x;
        final int screenHeight = screenDimensions.y;

        // Algoritmo iterativo para achar um tamanho final para a imagem
        while (shouldScaleDown && (imageRealWidth >= screenWidth || imageRealHeight >= screenHeight)) {
            imageRealWidth *= 0.9;
            imageRealHeight *= 0.9;
            requireResizingOfBitmap = true;
        }

        while (shouldScaleUp && ((imageRealWidth * 1.1) <= screenWidth && (imageRealHeight * 1.1) <= screenHeight)) {
            imageRealWidth *= 1.1;
            imageRealHeight *= 1.1;
            requireResizingOfBitmap = true;
        }

        int finalImageWidth = imageRealWidth;
        int finalImageHeight = imageRealHeight;

        if (requireResizingOfBitmap) {
            Bitmap bitmap = drawableToBitmap(imageViewDrawable);
            BitmapDrawable resizedBitmapDrawable = new BitmapDrawable(
                    context.getResources(),
                    Bitmap.createScaledBitmap(bitmap, finalImageWidth, finalImageHeight, false));
            poppedImageView.setBackgroundDrawable(resizedBitmapDrawable);
        } else {
            poppedImageView.setBackgroundDrawable(imageViewDrawable);
        }
    }

    /**
     * Enable tap to show popup image dialog with alternative drawable than imageView's drawable
     *
     * @param context   Context
     * @param imageView Target Image View
     * @param drawable  Alternative Drawable
     */
    public void enablePopUpOnClick(final Activity context, final ImageView imageView, final Drawable drawable) {
        internalEnablePopUpOnClick(context, imageView, drawable);
    }

    public void enablePopUpOnClick(final Activity context, final ImageView imageView) {
        internalEnablePopUpOnClick(context, imageView);
    }

    public void enablePopUpOnClick(final Activity context, final ImageView imageView, final String imageUrl) {
        if (imageUrl != null && mAuthInterceptor.getToken() != null) {
            poppedImageView = new ImageView(context);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(16, 16, 16, 16);

            GlideUrl glideUrl = new GlideUrl(imageUrl,
                    new LazyHeaders.Builder().addHeader("Authorization", mAuthInterceptor.getToken()).build());
            mRequestManager.load(glideUrl).into(poppedImageView);
            dialog = new Dialog(context);
            dialog.requestWindowFeature((int) Window.FEATURE_NO_TITLE);
            dialog.setContentView(poppedImageView, lp);
            dialog.getWindow().setBackgroundDrawable(null); // Without this line there is a very small border around the image (1px)
            dialog.setCanceledOnTouchOutside(true); // Gingerbread support

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.show();
                }
            });
        }
    }

    private void internalEnablePopUpOnClick(final Activity context, final ImageView imageView, final Drawable drawable) {

        this.context = context;
        poppedImageView = new ImageView(context);

        dialog = new Dialog(context);
        dialog.requestWindowFeature((int) Window.FEATURE_NO_TITLE);
        dialog.setContentView(poppedImageView);
        dialog.getWindow().setBackgroundDrawable(null); // Without this line there is a very small border around the image (1px)
        dialog.setCanceledOnTouchOutside(true); // Gingerbread support

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (drawable != null) {
                    if (imageViewDrawable != drawable) {
                        cacheResizedDrawable(drawable, true, true);
                    }
                } else {
                    ImageView imageView = (ImageView) v;

                    if (imageViewDrawable != imageView.getDrawable()) {
                        cacheResizedDrawable(imageView.getDrawable(), true, true);
                    }
                }

                dialog.show();
            }
        });
    }

    private void internalEnablePopUpOnClick(final Activity context, final ImageView imageView) {
        internalEnablePopUpOnClick(context, imageView, null);
    }

    @SuppressWarnings("deprecation")
    private Point getScreenDimensions(Activity context) {
        // Get screen size
        Display display = context.getWindowManager().getDefaultDisplay();
        Point size = new Point();

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealSize(size);
        } else if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB_MR2) {
            display.getSize(size);
        } else {
            size.x = display.getWidth();
            size.y = display.getHeight();
        }
        return size;
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
