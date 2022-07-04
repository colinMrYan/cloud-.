package com.inspur.emmcloud.componentservice.selector;

import android.content.Context;

import com.inspur.emmcloud.componentservice.CoreService;

import java.util.ArrayList;

public interface FileSelectorService extends CoreService {
    //从相册选择多张图片
    void selectImagesFromAlbum(Context context);
    //从相册选择多段视频
    void selectVideosFromAlbum(Context context);
}
