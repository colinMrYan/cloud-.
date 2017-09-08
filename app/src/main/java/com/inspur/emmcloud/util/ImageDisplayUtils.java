package com.inspur.emmcloud.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.inspur.emmcloud.R;
import com.inspur.imp.plugin.camera.imagepicker.loader.ImagePickerLoader;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.L;


public class ImageDisplayUtils implements ImagePickerLoader {
    private DisplayImageOptions options;

    public ImageDisplayUtils(Integer defaultDrawableId) {
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(defaultDrawableId)
                .showImageOnFail(defaultDrawableId)
                .showImageOnLoading(defaultDrawableId)
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
    }


    public ImageDisplayUtils() {
    }

    public void displayImage(final ImageView imageView, String uri) {
        if (!StringUtils.isBlank(uri) && !uri.startsWith("http") && !uri.startsWith("file:") && !uri.startsWith("content:") && !uri.startsWith("assets:") && !uri.startsWith("drawable:")) {
            uri = "file://" + uri;
        }
        ImageLoader.getInstance().displayImage(uri, imageView, options);
    }

    public void displayImageByTag(final ImageView imageView, String uri,final Integer defaultDrawableId) {
        if (!StringUtils.isBlank(uri) && !uri.startsWith("http") && !uri.startsWith("file:") && !uri.startsWith("content:") && !uri.startsWith("assets:") && !uri.startsWith("drawable:")) {
            uri = "file://" + uri;
        }
        final String finalUri = uri;
        imageView.setTag(finalUri);
            ImageLoader.getInstance().loadImage(uri,  options, new ImageLoadingListener(){
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    String tagUri =(String ) imageView.getTag();
                    if (tagUri != null && tagUri.equals(finalUri)){
                        imageView.setImageResource(defaultDrawableId);
                    }
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    String tagUri =(String ) imageView.getTag();
                    if (tagUri != null && tagUri.equals(finalUri)){
                        imageView.setImageResource(defaultDrawableId);
                    }
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    String tagUri =(String ) imageView.getTag();
                    if (tagUri != null && tagUri.equals(finalUri)){
                        imageView.setImageBitmap(loadedImage);
                    }
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {
                    String tagUri =(String ) imageView.getTag();
                    if (tagUri != null && tagUri.equals(finalUri)){
                        imageView.setImageResource(defaultDrawableId);
                    }
                }
            });

    }

    public void displayImageNoCache(final ImageView imageView, String uri, Integer defaultDrawableId) {
        options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(defaultDrawableId)
                .showImageOnFail(defaultDrawableId)
                //.showImageOnLoading(defaultDrawableId)
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .build();
        ImageLoader.getInstance().displayImage(uri, imageView, options);
    }

    @Override
    public void displayImage(Activity activity, String uri,
                             ImageView imageView, int width, int height) {
        // TODO Auto-generated method stub
        if (!StringUtils.isBlank(uri) && !uri.startsWith("http") && !uri.startsWith("file:") && !uri.startsWith("content:") && !uri.startsWith("assets:") && !uri.startsWith("drawable:")) {
            uri = "file://" + uri;
        }
        ImageSize size = new ImageSize(width, height);
        if (options == null) {
            options = new DisplayImageOptions.Builder()
                    // 设置图片的解码类型
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .showImageForEmptyUri(R.drawable.default_image)
                    .showImageOnFail(R.drawable.default_image)
                    .showImageOnLoading(R.drawable.default_image)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .build();
        }
        ImageLoader.getInstance().displayImage(uri, new ImageViewAware(imageView), options, size, null, null);
    }

    @Override
    public void clearMemoryCache() {
        // TODO Auto-generated method stub
        ImageLoader.getInstance().clearMemoryCache();
    }

    public void clearAllCache() {
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().clearDiscCache();
    }
}
