package com.inspur.emmcloud.servcieimpl;

import android.content.Context;

import com.inspur.emmcloud.componentservice.selector.FileSelectorService;
import com.inspur.emmcloud.util.privates.PictureSelectorUtils;

public class FileSelectorServiceImpl implements FileSelectorService {

    @Override
    public void selectImagesFromAlbum(Context context) {
        PictureSelectorUtils.getInstance().openGalleryInDefault(context);
    }

    @Override
    public void selectVideosFromAlbum(Context context) {
    }
}
