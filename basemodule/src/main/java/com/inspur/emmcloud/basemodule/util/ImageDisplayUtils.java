package com.inspur.emmcloud.basemodule.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.UsingFreqLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;
import com.nostra13.universalimageloader.utils.L;
import com.nostra13.universalimageloader.utils.MemoryCacheUtils;

import java.io.File;
import java.util.List;


public class ImageDisplayUtils {
    private static ImageDisplayUtils mInstance;

    private ImageDisplayUtils() {
    }

    public static ImageDisplayUtils getInstance() {
        if (mInstance == null) {
            synchronized (ImageDisplayUtils.class) {
                if (mInstance == null) {
                    mInstance = new ImageDisplayUtils();
                }
            }
        }
        return mInstance;
    }

    public void initImageLoader(Context context, BaseImageDownloader imageDownloader, String cacheDirPath) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(context)
                .memoryCacheExtraOptions(2600, 2600)
                .defaultDisplayImageOptions(options)
                .imageDownloader(imageDownloader)
                .threadPoolSize(6)
                .threadPriority(Thread.NORM_PRIORITY - 1)
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(
                        new UsingFreqLimitedMemoryCache(5 * 1024 * 1024))
                .memoryCacheSizePercentage(13)
                .diskCacheSize(200 * 1024 * 1024)
                // You can pass your own memory cache implementation
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .diskCacheFileNameGenerator(new HashCodeFileNameGenerator());
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            File cacheDir = new File(cacheDirPath);
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            builder = builder.diskCache(new UnlimitedDiskCache(cacheDir));
        }

        ImageLoaderConfiguration config = builder.build();
        L.disableLogging(); // 关闭imageloader的疯狂的log
        ImageLoader.getInstance().init(config);

    }


    public void displayRoundedImage(final ImageView imageView, String uri, Integer defaultDrawableId, Context context, float dp) {

        if (!StringUtils.isBlank(uri) && !uri.startsWith("http") && !uri.startsWith("file:") && !uri.startsWith("content:") && !uri.startsWith("assets:") && !uri.startsWith("drawable:")) {
            uri = "file://" + uri;
        }
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(defaultDrawableId)
                .showImageOnFail(defaultDrawableId)
                .showImageOnLoading(defaultDrawableId)
                .cacheInMemory(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888)   //设置图片的解码类型
                .displayer(new RoundedBitmapDisplayer(DensityUtil.dip2px(context, dp)))
                .build();
        ImageLoader.getInstance().displayImage(uri, imageView, options);
    }

    public void displayImage(final ImageView imageView, String uri, Integer defaultDrawableId) {
        DisplayImageOptions options = getDefaultOptions(defaultDrawableId);
        if (!StringUtils.isBlank(uri) && !uri.startsWith("http") && !uri.startsWith("file:") && !uri.startsWith("content:") && !uri.startsWith("assets:") && !uri.startsWith("drawable:")) {
            uri = "file://" + uri;
        }
        ImageLoader.getInstance().displayImage(uri, imageView, options);
    }

    public void displayImage(final ImageView imageView, String uri) {
        if (!StringUtils.isBlank(uri) && !uri.startsWith("http") && !uri.startsWith("file:") && !uri.startsWith("content:") && !uri.startsWith("assets:") && !uri.startsWith("drawable:")) {
            uri = "file://" + uri;
        }
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoader.getInstance().displayImage(uri, imageView, options);
    }

    /**
     * 消息页面头像展示使用此方法，为了刷新时不闪烁
     *
     * @param imageView
     * @param uri
     * @param defaultDrawableId
     */
    public void displayImageByTag(final ImageView imageView, String uri, final Integer defaultDrawableId) {
        if (StringUtils.isBlank(uri)) {
            imageView.setTag("");
            imageView.setImageResource(defaultDrawableId);
            return;
        }
        DisplayImageOptions options = getDefaultOptions(defaultDrawableId);
        if (!uri.startsWith("http") && !uri.startsWith("file:") && !uri.startsWith("content:") && !uri.startsWith("assets:") && !uri.startsWith("drawable:")) {
            uri = "file://" + uri;
        }
        final String finalUri = uri;
        imageView.setTag(finalUri);
        ImageLoader.getInstance().loadImage(uri, options, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                String tagUri = (String) imageView.getTag();
                if (tagUri != null && tagUri.equals(finalUri)) {
                    imageView.setImageResource(defaultDrawableId);
                }
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                String tagUri = (String) imageView.getTag();
                if (tagUri != null && tagUri.equals(finalUri)) {
                    imageView.setImageResource(defaultDrawableId);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                String tagUri = (String) imageView.getTag();
                if (tagUri != null && tagUri.equals(finalUri)) {
                    imageView.setImageBitmap(loadedImage);
                }
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                String tagUri = (String) imageView.getTag();
                if (tagUri != null && tagUri.equals(finalUri)) {
                    displayImageByTag(imageView, finalUri, defaultDrawableId);
                }
            }
        });

    }

    /**
     * 展示不缓存的图片
     *
     * @param imageView
     * @param uri
     * @param defaultDrawableId
     */
    public void displayImageNoCache(final ImageView imageView, String uri, Integer defaultDrawableId) {
        DisplayImageOptions options = getNoCacheOptions(defaultDrawableId);
        ImageLoader.getInstance().displayImage(uri, imageView, options);
    }


    /**
     * 展示不缓存的图片
     *
     * @param imageView
     * @param uri
     */
    public void displayImageNoCache(final ImageView imageView, String uri) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .build();
        ImageLoader.getInstance().displayImage(uri, imageView, options);
    }

    /**
     * 展示固定大小的图片
     * @param uri
     * @param imageView
     * @param width
     * @param height
     * @param defaultDrawableId
     */
    public void displayImage(String uri,
                             ImageView imageView, int width, int height, Integer defaultDrawableId) {
        // TODO Auto-generated method stub
        if (!StringUtils.isBlank(uri) && !uri.startsWith("http") && !uri.startsWith("file:") && !uri.startsWith("content:") && !uri.startsWith("assets:") && !uri.startsWith("drawable:")) {
            uri = "file://" + uri;
        }
        ImageSize size = new ImageSize(width, height);
        DisplayImageOptions options = getDefaultOptions(defaultDrawableId);
        ImageLoader.getInstance().displayImage(uri, new ImageViewAware(imageView), options, size, null, null);
    }

    /**
     * 获取默认的DisplayImageOptions
     *
     * @param defaultDrawableId
     * @return
     */
    public DisplayImageOptions getDefaultOptions(Integer defaultDrawableId) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565)
                .showImageForEmptyUri(defaultDrawableId)
                .showImageOnFail(defaultDrawableId)
                .showImageOnLoading(defaultDrawableId)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        return options;
    }


    /**
     * 获取无缓存的DisplayImageOptions
     *
     * @param defaultDrawableId
     * @return
     */
    public DisplayImageOptions getNoCacheOptions(Integer defaultDrawableId) {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(defaultDrawableId)
                .showImageOnFail(defaultDrawableId)
                //.showImageOnLoading(defaultDrawableId)
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(false)
                .cacheOnDisk(false)
                .build();
        return options;
    }


    public void clearAllCache() {
        ImageLoader.getInstance().clearMemoryCache();
        ImageLoader.getInstance().clearDiscCache();
    }

    public void clearCache(String url) {
        DiskCacheUtils.removeFromCache(url, ImageLoader.getInstance().getDiskCache());
        MemoryCacheUtils.removeFromCache(url, ImageLoader.getInstance().getMemoryCache());
    }

    public boolean isHaveImage(String url) {
        File imageFileCatch = DiskCacheUtils.findInCache(url, ImageLoader.getInstance().getDiskCache());
        List<String> imageCatchs = MemoryCacheUtils.findCacheKeysForImageUri(url, ImageLoader.getInstance().getMemoryCache());
        if (imageFileCatch == null) {
            return false;
        } else {
            return true;
        }
    }

}