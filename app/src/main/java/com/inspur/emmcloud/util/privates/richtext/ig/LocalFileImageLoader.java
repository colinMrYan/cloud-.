package com.inspur.emmcloud.util.privates.richtext.ig;

import android.widget.TextView;

import com.inspur.emmcloud.util.privates.richtext.ImageHolder;
import com.inspur.emmcloud.util.privates.richtext.RichTextConfig;
import com.inspur.emmcloud.util.privates.richtext.callback.ImageLoadNotify;
import com.inspur.emmcloud.util.privates.richtext.drawable.DrawableWrapper;
import com.inspur.emmcloud.util.privates.richtext.exceptions.ImageDecodeException;

/**
 * Created by zhou on 2017/2/20.
 * 本地图片加载器
 */

class LocalFileImageLoader extends AbstractImageLoader<String> implements Runnable {

    LocalFileImageLoader(ImageHolder holder, RichTextConfig config, TextView textView, DrawableWrapper drawableWrapper, ImageLoadNotify iln) {
        super(holder, config, textView, drawableWrapper, iln, SourceDecode.LOCAL_FILE_SOURCE_DECODE);
    }

    @Override
    public void run() {
        try {
            doLoadImage(holder.getSource());
        } catch (Exception e) {
            onFailure(new ImageDecodeException(e));
        } catch (OutOfMemoryError error) {
            onFailure(new ImageDecodeException(error));
        }
    }
}
