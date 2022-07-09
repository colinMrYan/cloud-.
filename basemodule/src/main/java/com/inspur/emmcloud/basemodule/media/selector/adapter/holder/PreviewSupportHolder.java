package com.inspur.emmcloud.basemodule.media.selector.adapter.holder;

import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.inspur.emmcloud.basemodule.media.selector.photoview.PhotoView;


/**
 * Date：2022/6/30
 * Author：wang zhen
 * Description 仿照RecyclerView holder 用于ViewPager
 */
public class PreviewSupportHolder {

    public PhotoView photoView;
    public ImageView playIv;
    public boolean isPicType;

    public PreviewSupportHolder(@NonNull PhotoView photoView, ImageView playIv, boolean picType) {
        this.photoView = photoView;
        this.playIv = playIv;
        this.isPicType = picType;
    }

    // 释放视频资源
    public void releaseVideo() {

    }
}
