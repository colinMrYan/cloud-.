package com.inspur.emmcloud.basemodule.util.imagepicker.loader;

import android.widget.ImageView;

import java.io.Serializable;

/**
 * ImageLoader抽象类，外部需要实现这个类去加载图片， 尽力减少对第三方库的依赖
 */
public interface ImagePickerLoader extends Serializable {

    void displayImage(String path, ImageView imageView, int width, int height, Integer defaultDrawableId);

    void clearMemoryCache();
}
