package com.inspur.emmcloud.widget.largeimage;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

import com.inspur.emmcloud.widget.largeimage.factory.BitmapDecoderFactory;


/**
 * Created by LuckyJayce on 2016/11/24.
 */
public interface ILargeImageView {

    int getImageWidth();

    int getImageHeight();

    boolean hasLoad();

    void setImage(BitmapDecoderFactory factory);

    void setImage(BitmapDecoderFactory factory, Drawable defaultDrawable);

    void setImage(Bitmap bm);

    void setImage(Drawable drawable);

    void setImage(@DrawableRes int resId);

    void setImageDrawable(Drawable drawable);

    float getScale();

    void setScale(float scale);

    BlockImageLoader.OnImageLoadListener getOnImageLoadListener();

    void setOnImageLoadListener(BlockImageLoader.OnImageLoadListener onImageLoadListener);
}
