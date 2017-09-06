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
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
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
//        ImageLoader.getInstance().displayImage(uri, imageView, options, new SimpleImageLoadingListener() {
//            @Override
//            public void onLoadingComplete(String imageUri, View view,
//                                          Bitmap loadedImage) {
//                FadeInBitmapDisplayer.animate(imageView, 800);
//                super.onLoadingComplete(imageUri, view, loadedImage);
//            }
//        });
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


//	public void displayPic(final ImageView imageView,String uri){
//		if (!StringUtils.isBlank(uri) && !uri.startsWith("http") && !uri.startsWith("file:")&& !uri.startsWith("content:")&& !uri.startsWith("assets:")&& !uri.startsWith("drawable:")) {
//			uri = "file://" + uri;
//		}
//		ImageLoader.getInstance().displayImage(uri, imageView,options,new SimpleImageLoadingListener(){
//			@Override
//			public void onLoadingComplete(String imageUri, View view,
//					Bitmap loadedImage) {
//				FadeInBitmapDisplayer.animate(imageView, 800);
//				super.onLoadingComplete(imageUri, view, loadedImage);
//			}
//		});
//	}

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
                    .showImageForEmptyUri(R.mipmap.default_image)
                    .showImageOnFail(R.mipmap.default_image)
                    .showImageOnLoading(R.mipmap.default_image)
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
