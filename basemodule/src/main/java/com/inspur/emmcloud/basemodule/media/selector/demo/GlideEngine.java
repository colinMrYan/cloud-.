package com.inspur.emmcloud.basemodule.media.selector.demo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaderFactory;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.selector.engine.ImageEngine;
import com.inspur.emmcloud.basemodule.media.selector.interfaces.OnCallbackListener;
import com.inspur.emmcloud.basemodule.media.selector.utils.ActivityCompatHelper;

/**
 * @author：luck
 * @date：2019-11-13 17:02
 * @describe：Glide加载引擎
 */
public class GlideEngine implements ImageEngine {

    /**
     * 加载图片
     *
     * @param context   上下文
     * @param url       资源url
     * @param imageView 图片承载控件
     */
    @Override
    public void loadImage(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .into(imageView);
    }

    /**
     * 加载指定url并返回bitmap
     *
     * @param context   上下文
     * @param url       资源url
     * @param maxWidth  资源最大加载尺寸
     * @param maxHeight 资源最大加载尺寸
     * @param call      回调接口
     */
    @Override
    public void loadImageBitmap(@NonNull Context context, @NonNull String url, int maxWidth, int maxHeight, final OnCallbackListener<Bitmap> call) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context)
                .asBitmap()
                .override(maxWidth, maxHeight)
                .load(url)
                .into(new CustomTarget<Bitmap>() {


                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        if (call != null) {
                            call.onCall(resource);
                        }
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        if (call != null) {
                            call.onCall(null);
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }

                });
    }

    /**
     * 加载相册目录封面
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    @Override
    public void loadAlbumCover(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context)
                .asBitmap()
                .load(url)
                .override(180, 180)
                .sizeMultiplier(0.5f)
                .transform(new CenterCrop(), new RoundedCorners(8))
                .placeholder(R.drawable.ps_image_placeholder)
                .into(imageView);
    }


    /**
     * 加载图片列表图片
     *
     * @param context   上下文
     * @param url       图片路径
     * @param imageView 承载图片ImageView
     */
    @Override
    public void loadGridImage(@NonNull Context context, @NonNull String url, @NonNull ImageView imageView) {
        if (!ActivityCompatHelper.assertValidRequest(context)) {
            return;
        }
        Glide.with(context)
                .load(url)
                .override(200, 200)
                .centerCrop()
                .placeholder(R.drawable.ps_image_placeholder)
                .into(imageView);
    }

    /**
     * 加载视频首帧图片
     *
     * @param context   上下文
     * @param videoPath 播放路径
     * @param imageView 承载图片ImageView
     */
    public void loadVideoThumbnailImage(@NonNull Context context, @NonNull String videoPath, int maxWidth,
                                        int maxHeight, @NonNull ImageView imageView, int holder, RequestListener<Drawable> requestListener) {
        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().frame(0).centerCrop())
                .load(videoPath)
//                .override(maxWidth, maxHeight)
                .listener(requestListener)
                .placeholder(holder)
                .into(imageView);
    }

    /**
     * 加载视频首帧图片
     *
     * @param context   上下文
     * @param videoPath 播放路径
     * @param imageView 承载图片ImageView
     */
    public void loadVideoThumbnailImageWithHeader(@NonNull final Context context, @NonNull final String videoPath, int maxWidth,
                                                  int maxHeight, @NonNull final ImageView imageView, final int holder) {
//        String versionValue = AppUtils.getVersion(BaseApplication.getInstance());
//        try {
//            Version version = Version.valueOf(versionValue);
//            versionValue = version.getNormalVersion();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        final LazyHeaders build = new LazyHeaders.Builder()
//                .setHeader("Authorization", BaseApplication.getInstance().getToken())
//                .setHeader("X-ECC-Current-Enterprise", BaseApplication.getInstance().getCurrentEnterprise().getId())
//                .setHeader("Content-Type", "application/x-www-form-urlencoded")
//                .setHeader("Content-Disposition", "inline")
//                .setHeader("User-Agent", "Android/" + AppUtils.getReleaseVersion() + "("
//                        + AppUtils.GetChangShang() + " " + AppUtils.GetModel()
//                        + ") " + "CloudPlus_Phone/"
//                        + versionValue)
//                .build();
//
//        GlideUrl glideUrl = new GlideUrl(videoPath, build);
//        Glide.with(context)
//                .setDefaultRequestOptions(new RequestOptions().frame(0).centerCrop())
//                .load(glideUrl)
////                .override(maxWidth, maxHeight)
//                .placeholder(holder)
//                .listener(new RequestListener<Drawable>() {
//                    @Override
//                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//
////                        GlideUrl glideUrl = new GlideUrl(videoPath, build);
////                        Glide.with(context)
////                                .setDefaultRequestOptions(new RequestOptions().frame(0).centerCrop())
////                                .load(glideUrl)
//////                .override(maxWidth, maxHeight)
////                                .placeholder(holder)
////                                .into(imageView);
//                        return false;
//                    }
//
//                    @Override
//                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                        return false;
//                    }
//                })
//                .into(imageView)
//        ;
    }

    @Override
    public void pauseRequests(Context context) {
        Glide.with(context).pauseRequests();
    }

    @Override
    public void resumeRequests(Context context) {
        Glide.with(context).resumeRequests();
    }

    private GlideEngine() {
    }

    private static GlideEngine instance;

    public static GlideEngine createGlideEngine() {
        if (null == instance) {
            synchronized (GlideEngine.class) {
                if (null == instance) {
                    instance = new GlideEngine();
                }
            }
        }
        return instance;
    }
}
