package com.inspur.emmcloud.widget.largeimage.factory;

import android.graphics.BitmapRegionDecoder;

import java.io.IOException;

public interface BitmapDecoderFactory {
    BitmapRegionDecoder made() throws IOException;

    int[] getImageInfo();
}