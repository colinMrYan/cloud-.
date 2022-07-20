package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ResourceUtils;
import com.inspur.emmcloud.baselib.widget.CustomLoadingView;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.itheima.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

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
                R.layout.chat_msg_card_child_res_img_view, null);
        final RoundedImageView imageView = (RoundedImageView) cardContentView
                .findViewById(R.id.content_img);
//        final CustomLoadingView loadingView = cardContentView.findViewById(R.id.qlv_downloading_left);
        MsgContentMediaImage msgContentMediaImage = message.getMsgContentMediaImage();
        String imageUri = msgContentMediaImage.getRawMedia();
        int chatImgBg = ResourceUtils.getResValueOfAttr(context, R.attr.bg_chat_img);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(chatImgBg)
                .showImageOnFail(chatImgBg)
                .showImageOnLoading(chatImgBg)
                .considerExifParams(true)
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565).cacheInMemory(true)
                .cacheOnDisk(true).build();
        if (!imageUri.startsWith("http") && !imageUri.startsWith("file:") && !imageUri.startsWith("content:") && !imageUri.startsWith("assets:") && !imageUri.startsWith("drawable:")) {
            if (uiMessage.getSendStatus() == 1) {
                imageUri = APIUri.getChatFileResouceUrl(message.getChannel(), imageUri);
            } else {
                imageUri = "file://" + imageUri;
            }

        }
        //判断是否有Preview 图片如果有的话用preview ，否则用原图
        int w = msgContentMediaImage.getRawWidth();
        int h = msgContentMediaImage.getRawHeight();
        if (msgContentMediaImage.getPreviewHeight() > 0 && msgContentMediaImage.getPreviewWidth() > 0) {
            h = msgContentMediaImage.getPreviewHeight();
            w = msgContentMediaImage.getPreviewWidth();
        }
        final boolean isHasSetImageViewSize = setImgViewSize(context, imageView, w, h);
        if (!ImageDisplayUtils.getInstance().isHaveCacheImage(imageUri) && imageUri.startsWith("http") &&
                msgContentMediaImage.getPreviewHeight() != 0
                && (msgContentMediaImage.getRawHeight() != msgContentMediaImage.getPreviewHeight())) {
            imageUri = imageUri + "&resize=true&w=" + message.getMsgContentMediaImage().getPreviewWidth() +
                    "&h=" + message.getMsgContentMediaImage().getPreviewHeight();
        }
        LogUtils.LbcDebug("DisplayImgUri::" + imageUri + "::::size:" + "getRawHeight()" + msgContentMediaImage.getRawHeight() + "getPreviewHeight()" + msgContentMediaImage.getPreviewHeight());
        ImageLoader.getInstance().displayImage(imageUri, imageView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
//                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view,
                                          Bitmap loadedImage) {
                FadeInBitmapDisplayer.animate(imageView, 800);
                int w = loadedImage.getWidth();
                int h = loadedImage.getHeight();
                if (!isHasSetImageViewSize) {
                    setImgViewSize(context, imageView, w, h);
                }
//                loadingView.setVisibility(View.GONE);
            }
        });
        return cardContentView;

    }

    /**
     * 设置imageView的尺寸
     *
     * @param context
     * @param imageView
     * @param longImgText
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


    /**
     * 获取ImageView的大小
     *
     * @param context
     * @param w       // 30~130
     * @param h       // 30~130
     * @return
     */
    public static LayoutParams getImgViewSize(Context context, int w, int h) {
        LayoutParams params = new LayoutParams(0, 0);
        if (w == 0 || h == 0) {
            return params;
        }
        int minW = DensityUtil.dip2px(context, 60);
        int minH = DensityUtil.dip2px(context, 60);
        int maxW = DensityUtil.dip2px(context, 260);
        int maxH = DensityUtil.dip2px(context, 260);
        if (w == h) {
            params.width = minW;
            params.height = minW;
        } else if (h > w) {
            params.width = minW;
            params.height = (int) (minW * 1.0 * h / w);
            if (params.height > maxH) {
                params.height = maxH;
            }
        } else {
            params.width = maxW;
            params.height = (int) (maxW * 1.0 * h / w);
            if (params.height < minH) {
                params.height = minH;
            }
        }
        return params;
    }

}
