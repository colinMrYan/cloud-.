package com.inspur.emmcloud.util.privates.richtext.callback;


import com.inspur.emmcloud.util.privates.richtext.ImageHolder;

/**
 * Created by zhou on 2017/2/21.
 * SimpleImageFixCallback
 */

public abstract class SimpleImageFixCallback implements ImageFixCallback {

    @Override
    public void onInit(ImageHolder holder) {

    }

    @Override
    public void onLoading(ImageHolder holder) {

    }

    @Override
    public void onSizeReady(ImageHolder holder, int imageWidth, int imageHeight, ImageHolder.SizeHolder sizeHolder) {

    }

    @Override
    public void onImageReady(ImageHolder holder, int width, int height) {

    }

    @Override
    public void onFailure(ImageHolder holder, Exception e) {

    }
}
