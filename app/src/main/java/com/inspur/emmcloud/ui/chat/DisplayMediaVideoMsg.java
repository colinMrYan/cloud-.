package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.engine.LoadPath;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.api.BaseModuleAPIInterfaceInstance;
import com.inspur.emmcloud.basemodule.api.BaseModuleApiService;
import com.inspur.emmcloud.basemodule.media.player.model.SuperPlayerModel;
import com.inspur.emmcloud.basemodule.media.selector.demo.GlideEngine;
import com.inspur.emmcloud.basemodule.media.selector.thread.PictureThreadUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaVideo;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
import com.itheima.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * DisplayMediaVideoMsg
 *
 * @author 视频卡片
 */
public class DisplayMediaVideoMsg {
    /**
     * 展示视频资源卡片
     *
     * @param context
     */
    public static View getView(final Activity context,
                               final UIMessage uiMessage) {
        final Message message = uiMessage.getMessage();
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_res_video_view, null);
        final RoundedImageView imageView = (RoundedImageView) cardContentView
                .findViewById(R.id.content_img);
        TextView durationTv = cardContentView.findViewById(R.id.tv_duration);
        final MsgContentMediaVideo msgContentMediaVideo = message.getMsgContentMediaVideo();
        durationTv.setText(formattedTime(msgContentMediaVideo.getVideoDuration()));
        final int chatImgBg = ResourceUtils.getResValueOfAttr(context, R.attr.bg_chat_img);

        //判断是否有Preview 图片如果有的话用preview ，否则用原图
        int w = msgContentMediaVideo.getImageWidth();
        int h = msgContentMediaVideo.getImageHeight();
        setImgViewSize(context, imageView, w, h);
        String imagePath = msgContentMediaVideo.getImagePath();
        String localPath = message.getLocalPath();
        // 先使用缓存，再使用本地加载，再请求网络
        if (imagePath.startsWith("http")) {
            GlideEngine.createGlideEngine().loadVideoThumbnailImage(context, imagePath, 0, 0, imageView, chatImgBg, new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    if (e != null && e.getMessage().contains("FileNotFoundException")) {
                        // 防止Glide缓存失效，重新请求
                        JSONObject object = JSONUtils.getJSONObject(message.getContent());
                        JSONObject picInfo = JSONUtils.getJSONObject(object, "thumbnail", new JSONObject());
                        try {
                            picInfo.put("media", "");
                        } catch (JSONException jsonException) {
                            jsonException.printStackTrace();
                        }
                        message.setContent(object.toString());
                        MessageCacheUtil.saveMessage(context, message);
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            });
        } else {
            // 防止图片本地被删除，不为空再加载本地资源
            File file = new File(localPath);
            if (!TextUtils.isEmpty(localPath) && file.exists()) {
                GlideEngine.createGlideEngine().loadVideoThumbnailImage(context, localPath, 0, 0, imageView, chatImgBg, null);
            } else {
                imageView.setImageResource(chatImgBg);
                BaseModuleApiService appAPIService = new BaseModuleApiService(context);
                appAPIService.setAPIInterface(new BaseModuleAPIInterfaceInstance() {
                    @Override
                    public void returnVideoSuccess(final String url) {
                        PictureThreadUtils.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                JSONObject object = JSONUtils.getJSONObject(message.getContent());
                                JSONObject picInfo = JSONUtils.getJSONObject(object, "thumbnail", new JSONObject());
                                try {
                                    picInfo.put("media", url);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                message.setContent(object.toString());
                                MessageCacheUtil.saveMessage(context, message);
                                GlideEngine.createGlideEngine().loadVideoThumbnailImage(context, url, 0, 0, imageView, chatImgBg, null);
                            }
                        });
                    }

                    @Override
                    public void returnVideoFail(String error, int errorCode) {
                    }

                });
                String originUlr = APIUri.getECMChatUrl() + "/api/v1/channel/" + message.getChannel() + "/file/request?path=" + StringUtils.encodeURIComponent(msgContentMediaVideo.getMedia()) + "&inlineContent=true";
                appAPIService.getVideoUrl(originUlr);
            }
        }
        return cardContentView;

    }

    /**
     * 设置imageView的尺寸
     *
     * @param context
     * @param imageView
     * @param w
     * @param h
     * @return
     */
    private static boolean setImgViewSize(Activity context, ImageView imageView, int w, int h) {
        if (w == 0 || h == 0) {
            return false;
        }
        int minW = DensityUtil.dip2px(context, 60);
        int minH = DensityUtil.dip2px(context, 60);
        int maxW = DensityUtil.dip2px(context, 130);
        int maxH = DensityUtil.dip2px(context, 130);
        LayoutParams params = imageView.getLayoutParams();
        if (w == h) {
            params.width = maxW;
            params.height = maxH;
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else if (h > w) {
            params.height = maxH;
            params.width = (int) (maxH * 1.0 * w / h);
            if (params.width < minW) {
                params.width = minW;
            }
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            params.width = maxW;
            params.height = (int) (maxW * 1.0 * h / w);
            if (params.height < minH) {
                params.height = minH;
            }
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        imageView.setLayoutParams(params);
        return true;
    }

    private static String formattedTime(long second) {
        String formatTime;
        long h, m, s;
        h = second / 3600;
        m = (second % 3600) / 60;
        s = (second % 3600) % 60;
        if (h == 0) {
            formatTime = asTwoDigit(m) + ":" + asTwoDigit(s);
        } else {
            formatTime = asTwoDigit(h) + ":" + asTwoDigit(m) + ":" + asTwoDigit(s);
        }
        return formatTime;
    }

    private static String asTwoDigit(long digit) {
        String value = "";
        if (digit < 10) {
            value = "0";
        }
        value += String.valueOf(digit);
        return value;
    }

}
