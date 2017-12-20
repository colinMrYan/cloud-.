package com.inspur.emmcloud.util.common.richtext.ig;

import android.widget.TextView;

import com.inspur.emmcloud.util.common.richtext.ImageHolder;
import com.inspur.emmcloud.util.common.richtext.RichTextConfig;
import com.inspur.emmcloud.util.common.richtext.callback.ImageLoadNotify;
import com.inspur.emmcloud.util.common.richtext.drawable.DrawableWrapper;
import com.inspur.emmcloud.util.common.richtext.exceptions.BitmapCacheLoadFailureException;
import com.inspur.emmcloud.util.common.richtext.exceptions.BitmapCacheNotfoudException;

/**
 * Created by zhou on 2017/3/25.
 * 本地缓存图片加载器
 */
class LocalDiskCachedImageLoader extends AbstractImageLoader implements Runnable {

    LocalDiskCachedImageLoader(ImageHolder holder, RichTextConfig config, TextView textView, DrawableWrapper drawableWrapper, ImageLoadNotify iln) {
        //noinspection unchecked
        super(holder, config, textView, drawableWrapper, iln, null, null);
    }


    @Override
    public void run() {
        int exist = BitmapPool.getPool().exist(holder.getKey());
        if (exist < 1) {
            onFailure(new BitmapCacheNotfoudException());
        } else {
            BitmapWrapper bitmapWrapper = BitmapPool.getPool().read(holder.getKey(), true);
            if (bitmapWrapper == null) {
                onFailure(new BitmapCacheLoadFailureException());
            } else {
                sizeCacheHolder = bitmapWrapper.getSizeCacheHolder();
                onResourceReady(ImageWrapper.createAsBitmap(bitmapWrapper.getBitmap()));
            }
        }
    }
}
