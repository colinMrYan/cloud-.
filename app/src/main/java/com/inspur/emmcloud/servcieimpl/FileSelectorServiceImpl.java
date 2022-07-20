package com.inspur.emmcloud.servcieimpl;

import android.support.v4.app.Fragment;

import com.inspur.emmcloud.basemodule.media.selector.config.SelectMimeType;
import com.inspur.emmcloud.basemodule.util.pictureselector.PictureSelectorUtils;
import com.inspur.emmcloud.componentservice.selector.FileSelectorService;

public class FileSelectorServiceImpl implements FileSelectorService {

    @Override
    public void selectImagesFromAlbum(Fragment fragment, int selectMaxNum, int fileType, int resultNum) {
//        PictureSelectorUtils.getInstance().openGallery(fragment, fileType == 0 ? SelectMimeType.ofImage() : SelectMimeType.ofVideo(), selectMaxNum, selectMaxNum, resultNum);
    }
}
