package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIUri;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.bean.chat.Msg;
import com.inspur.emmcloud.util.privates.cache.MsgCacheUtil;
import com.inspur.emmcloud.widget.CustomLoadingView;
import com.itheima.roundedimageview.RoundedImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.io.Serializable;
import java.util.List;

/**
 * DisplayResImageMsg
 *
 * @author sunqx 展示图片卡片 2016-08-19
 */
public class DisplayResImageMsg {
    /**
     * 展示图片资源卡片
     *
     * @param context
     * @param convertView
     * @param msg
     */
    public static View displayResImgMsg(final Activity context,
                                        final Msg msg) {
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_res_img_view, null);
        final RoundedImageView imageView = (RoundedImageView) cardContentView
                .findViewById(R.id.content_img);
        final CustomLoadingView loadingView = cardContentView.findViewById(R.id.qlv_downloading_left);
        final TextView longImgText = (TextView) cardContentView.findViewById(R.id.long_img_text);
        String imageUri = JSONUtils.getString(msg.getBody(), "key", "");
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.icon_photo_default)
                .showImageOnFail(R.drawable.icon_photo_default)
                .showImageOnLoading(R.drawable.icon_photo_default)
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565).cacheInMemory(true)
                .cacheOnDisk(true).build();
        if (!imageUri.startsWith("http") && !imageUri.startsWith("file:") && !imageUri.startsWith("content:") && !imageUri.startsWith("assets:") && !imageUri.startsWith("drawable:")) {
            if (msg.getSendStatus() == 1) {
                imageUri = APIUri.getPreviewUrl(imageUri);
            } else {
                imageUri = "file://" + imageUri;
            }
        }
        String body = msg.getBody();
        int w = JSONUtils.getInt(body, "width", 0);
        int h = JSONUtils.getInt(body, "height", 0);
        final boolean isHasSetImageViewSize = setImgViewSize(context, imageView, longImgText, w, h);

        ImageLoader.getInstance().displayImage(imageUri, imageView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
                loadingView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view,
                                          Bitmap loadedImage) {
                FadeInBitmapDisplayer.animate(imageView, 800);
                int w = loadedImage.getWidth();
                int h = loadedImage.getHeight();
                if (!isHasSetImageViewSize) {
                    setImgViewSize(context, imageView, longImgText, w, h);
                }
                loadingView.setVisibility(View.GONE);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (msg.getSendStatus() != 1) {
                    return;
                }
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                view.invalidate();
                int width = view.getWidth();
                int height = view.getHeight();
                Intent intent = new Intent(context,
                        ImagePagerV0Activity.class);
                List<Msg> imgTypeMsgList = MsgCacheUtil.getImgTypeMsgList(context, msg.getCid(), false);
                intent.putExtra(ImagePagerV0Activity.EXTRA_IMAGE_MSG_LIST, (Serializable) imgTypeMsgList);
                intent.putExtra(ImagePagerV0Activity.EXTRA_CURRENT_IMAGE_MSG, msg);
                intent.putExtra(ImagePagerV0Activity.PHOTO_SELECT_X_TAG, location[0]);
                intent.putExtra(ImagePagerV0Activity.PHOTO_SELECT_Y_TAG, location[1]);
                intent.putExtra(ImagePagerV0Activity.PHOTO_SELECT_W_TAG, width);
                intent.putExtra(ImagePagerV0Activity.PHOTO_SELECT_H_TAG, height);
                context.startActivity(intent);
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
    private static boolean setImgViewSize(Activity context, ImageView imageView, TextView longImgText, int w, int h) {
        if (w == 0 || h == 0) {
            return false;
        }
        int minW = DensityUtil.dip2px(context, 100);
        int minH = DensityUtil.dip2px(context, 90);
        int maxW = DensityUtil.dip2px(context, 270);
        int maxH = DensityUtil.dip2px(context, 232);
        LayoutParams params = imageView.getLayoutParams();
        if (w == h) {
            params.width = minW;
            params.height = minW;
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        } else if (h > w) {
            params.width = minW;
            params.height = (int) (minW * 1.0 * h / w);
            if (params.height > maxH) {
                longImgText.setVisibility(View.VISIBLE);
                params.height = maxH;
            }
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            params.width = maxW;
            params.height = (int) (maxW * 1.0 * h / w);
            if (params.height < minH) {
                params.height = minH;
                longImgText.setVisibility(View.VISIBLE);
            }
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        imageView.setLayoutParams(params);
        return true;
    }
}
