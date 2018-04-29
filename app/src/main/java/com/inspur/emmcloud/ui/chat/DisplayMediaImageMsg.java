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
import com.inspur.emmcloud.bean.chat.Message;
import com.inspur.emmcloud.bean.chat.MsgContentMediaImage;
import com.inspur.emmcloud.bean.chat.UIMessage;
import com.inspur.emmcloud.util.common.DensityUtil;
import com.inspur.emmcloud.util.privates.cache.MessageCacheUtil;
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
public class DisplayMediaImageMsg {
    /**
     * 展示图片资源卡片
     *
     * @param context
     * @param convertView
     * @param msg
     */
    public static View getView(final Activity context,
                                        final UIMessage UIMessage) {
        final Message message = UIMessage.getMessage();
        View cardContentView = LayoutInflater.from(context).inflate(
                R.layout.chat_msg_card_child_res_img_view, null);
        final ImageView imageView = (ImageView) cardContentView
                .findViewById(R.id.content_img);
        final TextView longImgText = (TextView) cardContentView.findViewById(R.id.long_img_text);
        MsgContentMediaImage msgContentMediaImage = message.getMsgContentMediaImage();
        String imageUri = msgContentMediaImage.getRawMedia();

        if (!imageUri.startsWith("content:") && !imageUri.startsWith("file:")) {
            imageUri = APIUri.getPreviewUrl(imageUri);

        }
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_image)
                .showImageOnFail(R.drawable.default_image)
                .showImageOnLoading(R.drawable.default_image)
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565).cacheInMemory(true)
                .cacheOnDisk(true).build();
        if (!imageUri.startsWith("http") && !imageUri.startsWith("file:") && !imageUri.startsWith("content:") && !imageUri.startsWith("assets:") && !imageUri.startsWith("drawable:")) {
            imageUri = "file://" + imageUri;
        }
        int w = msgContentMediaImage.getRawWidth();
        int h = msgContentMediaImage.getRawHeight();
        final boolean isHasSetImageViewSize = setImgViewSize(context, imageView, longImgText, w,h);
		ImageLoader.getInstance().displayImage(imageUri, imageView, options, new SimpleImageLoadingListener(){
			@Override
			public void onLoadingComplete(String imageUri, View view,
					Bitmap loadedImage) {
				FadeInBitmapDisplayer.animate(imageView, 800);
                if (!isHasSetImageViewSize){
                    int w = loadedImage.getWidth();
                    int h = loadedImage.getHeight();
                    setImgViewSize(context, imageView, longImgText, w,h);
                }
			}
		});

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (UIMessage.getSendStatus() != 1) {
                    return;
                }
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                view.invalidate();
                int width = view.getWidth();
                int height = view.getHeight();
                Intent intent = new Intent(context,
                        ChatImagePagerActivity.class);
                List<Message> imgTypeMsgList = MessageCacheUtil.getImgTypeMessageList(context, message.getChannel(), false);
                intent.putExtra(ImagePagerActivity.EXTRA_IMAGE_MSG_LIST, (Serializable) imgTypeMsgList);
                intent.putExtra(ImagePagerActivity.EXTRA_CURRENT_IMAGE_MSG, message);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_X_TAG, location[0]);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_Y_TAG, location[1]);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_W_TAG, width);
                intent.putExtra(ImagePagerActivity.PHOTO_SELECT_H_TAG, height);
                context.startActivity(intent);
            }
        });
        return cardContentView;

    }

    /**
     * 设置imageView的尺寸
     * @param context
     * @param imageView
     * @param longImgText
     * @param w
     * @param h
     * @return
     */
    private static boolean setImgViewSize(Activity context, ImageView imageView, TextView longImgText, int w,int h) {
        if (w == 0 || h == 0) {
            return false;
        }
        int minW = DensityUtil.dip2px(context, 116);
        int minH = DensityUtil.dip2px(context, 94);
        int maxW = DensityUtil.dip2px(context, 287);
        int maxH = DensityUtil.dip2px(context, 210);
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
        } else {
            params.width = maxW;
            params.height = (int) (maxW * 1.0 * h / w);
            if (params.height < minH) {
                params.height = minH;
                longImgText.setVisibility(View.VISIBLE);
            }
        }
        imageView.setLayoutParams(params);
        return true;
    }
}
