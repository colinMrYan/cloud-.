package com.inspur.emmcloud.util.privates.richtext.ig;

import android.graphics.Bitmap;

import com.inspur.emmcloud.util.privates.richtext.callback.BitmapStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by chenmch on 2019/4/23.
 */

public class MyBitmapStream implements BitmapStream {
    private  InputStream inputStream;
    private String source;
    public MyBitmapStream(String source){
        this.source = source;
    }
    @Override
    public void close() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        Bitmap bitmap = com.nostra13.universalimageloader.core.ImageLoader.getInstance().loadImageSync(source);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        inputStream = new ByteArrayInputStream(baos.toByteArray());
        return inputStream;
    }
}
