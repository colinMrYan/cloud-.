package com.inspur.emmcloud.ui.chat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.Msg;
import com.inspur.emmcloud.util.DensityUtil;
import com.inspur.emmcloud.util.JSONUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * DisplayResImageMsg
 * 
 * @author sunqx 展示图片卡片 2016-08-19
 */
public class DisplayResImageMsg {
	/**
	 * 展示图片资源卡片
	 * @param context
	 * @param convertView
	 * @param msg
     */
	public static void displayResImgMsg(final Activity context,
			View convertView, final Msg msg) {
		final ImageView imageView = (ImageView) convertView
				.findViewById(R.id.content_img);
		final TextView longImgText = (TextView) convertView.findViewById(R.id.long_img_text);
		String imageUri = JSONUtils.getString(msg.getBody(), "key", "");
		if (!imageUri.startsWith("content:") && !imageUri.startsWith("file:")) {
			imageUri = UriUtils.getPreviewUri(imageUri);

		}
		DisplayImageOptions options = new DisplayImageOptions.Builder()
				.showImageForEmptyUri(R.drawable.icon_photo_default)
				.showImageOnFail(R.drawable.icon_photo_default)
				.showImageOnLoading(R.drawable.icon_photo_default)
				// 设置图片的解码类型
				.bitmapConfig(Bitmap.Config.RGB_565).cacheInMemory(true)
				.cacheOnDisk(true).build();
		if (!imageUri.startsWith("http") && !imageUri.startsWith("file:")&& !imageUri.startsWith("content:")&& !imageUri.startsWith("assets:")&& !imageUri.startsWith("drawable:")) {
			imageUri = "file://" + imageUri;
		}
		ImageLoader.getInstance().displayImage(imageUri, imageView, options, new SimpleImageLoadingListener(){
			@Override
			public void onLoadingComplete(String imageUri, View view,
					Bitmap loadedImage) {
				FadeInBitmapDisplayer.animate(imageView, 800);
				int minW = DensityUtil.dip2px(context, 116);
				int minH = DensityUtil.dip2px(context, 94);
				int maxW = DensityUtil.dip2px(context, 287);
				int maxH = DensityUtil.dip2px(context, 210);
				float WHMaxRadio = (float) (maxW*1.0/minH);
				float HWMaxRadio = (float) (maxH*1.0/minW);
				int w = loadedImage.getWidth();
				int h = loadedImage.getHeight();
				LayoutParams params = imageView.getLayoutParams();
				if (w == h) {
					params.width = minW;
					params.height = minW;
					imageView.setScaleType(ImageView.ScaleType.FIT_XY);
				}else if (h>w) {
					params.width = minW;
					if (h>w*HWMaxRadio) {
						longImgText.setVisibility(View.VISIBLE);
					}
				}else {
					params.width = maxW;
					if (w>WHMaxRadio*h) {
						params.height = minH;
						longImgText.setVisibility(View.VISIBLE);
					}
				}
			 imageView.setLayoutParams(params);
			}
		});
		
	}
}
