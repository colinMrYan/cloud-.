package com.inspur.emmcloud.componentservice.selector;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.inspur.emmcloud.componentservice.CoreService;

import java.util.ArrayList;

public interface FileSelectorService extends CoreService {
    //从相册选择多张图片、视频
    void selectImagesFromAlbum(Fragment context, int selectMaxNum, int fileType, int resultNum);
}
