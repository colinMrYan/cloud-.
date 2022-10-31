package com.inspur.emmcloud.componentservice.selector;

import androidx.fragment.app.Fragment;

import com.inspur.emmcloud.componentservice.CoreService;

public interface FileSelectorService extends CoreService {
    //从相册选择多张图片、视频
    void selectImagesFromAlbum(Fragment context, int selectMaxNum, int fileType, int resultNum);
}
