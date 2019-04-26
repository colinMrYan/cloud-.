package com.inspur.emmcloud.util.common.richtext.imagegetter;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.util.common.richtext.ImageHolder;
import com.inspur.emmcloud.util.common.richtext.RichTextConfig;
import com.inspur.emmcloud.util.common.richtext.callback.ImageGetter;
import com.inspur.emmcloud.util.common.richtext.callback.ImageLoadNotify;
import com.inspur.emmcloud.util.privates.ImageDisplayUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by chenmch on 2019/4/22.
 */

public class ImageLoaderImageGetter implements ImageGetter {
    @Override
    public void recycle() {

    }

    @Override
    public Drawable getDrawable(ImageHolder holder, RichTextConfig config, TextView textView) {
        DisplayImageOptions options = ImageDisplayUtils.getInstance().getDefaultOptions(R.drawable.icon_person_default);
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync(holder.getSource(), options);
        return   new BitmapDrawable(textView.getContext().getResources(),bitmap);
    }

    @Override
    public void registerImageLoadNotify(ImageLoadNotify imageLoadNotify) {

    }
}
