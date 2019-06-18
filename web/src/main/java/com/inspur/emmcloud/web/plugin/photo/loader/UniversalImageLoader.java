package com.inspur.emmcloud.web.plugin.photo.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.inspur.emmcloud.baselib.util.LogUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.File;

/**
 * 使用 <a href="https://github.com/nostra13/Android-Universal-Image-Loader">
 * Android-Universal-Image-Loader</a>作为 Transferee 的图片加载器
 * <p>
 * Created by hitomi on 2017/5/3.
 * <p>
 * email: 196425254@qq.com
 */
public class UniversalImageLoader implements ImageLoaderCommon {
    private Context context;
    private DisplayImageOptions normalImageOptions;

    private UniversalImageLoader(Context context) {
        this.context = context;
    }

    public static UniversalImageLoader with(Context context) {
        return new UniversalImageLoader(context);
    }


    @Override
    public void showImage(String imageUrl, ImageView imageView, Drawable placeholder, final SourceCallback sourceCallback) {
        DisplayImageOptions options = new DisplayImageOptions
                .Builder()
                .showImageOnLoading(placeholder)
                .showImageOnFail(placeholder)
                .showImageForEmptyUri(placeholder)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .resetViewBeforeLoading(true)
                .build();
        ImageLoader.getInstance().displayImage(imageUrl, imageView, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                if (sourceCallback != null)
                    sourceCallback.onStart();
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                if (sourceCallback != null)
                    sourceCallback.onDelivered(STATUS_DISPLAY_FAILED);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                if (sourceCallback != null) {
                    sourceCallback.onFinish();
                    sourceCallback.onDelivered(STATUS_DISPLAY_SUCCESS);
                }

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                if (sourceCallback != null)
                    sourceCallback.onDelivered(STATUS_DISPLAY_CANCEL);
            }
        }, new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                LogUtils.jasonDebug("curremt==" + current);
                LogUtils.jasonDebug("total==" + total);
                if (sourceCallback != null)
                    sourceCallback.onProgress(current * 100 / total);
            }
        });
    }

    @Override
    public void loadImageAsync(String imageUrl, final ThumbnailCallback callback) {
        ImageLoader.getInstance().loadImage(imageUrl, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                callback.onFinish(null);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                callback.onFinish(new BitmapDrawable(context.getResources(), loadedImage));
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                callback.onFinish(null);
            }
        });
    }

    @Override
    public Drawable loadImageSync(String imageUrl) {
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(imageUrl, normalImageOptions);
        return new BitmapDrawable(bitmap);
    }

    @Override
    public boolean isLoaded(String url) {
        File cache = ImageLoader.getInstance().getDiskCache().get(url);
        return cache != null && cache.exists();
    }

    @Override
    public void clearCache() {
        ImageLoader.getInstance().getMemoryCache().clear();
        ImageLoader.getInstance().getDiskCache().clear();
    }
}
