package com.inspur.emmcloud.util.common.richtext.ig;

import com.inspur.emmcloud.util.common.richtext.callback.BitmapStream;

import java.io.IOException;

/**
 * Created by chenmch on 2019/4/23.
 */

public class MyImageDownloader implements ImageDownloader{
    @Override
    public BitmapStream download(final String source) throws IOException {
        return new MyBitmapStream(source);
    }

}
