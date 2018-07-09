package com.inspur.imp.plugin.photo;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.imp.plugin.photo.loader.UniversalImageLoader;
import com.inspur.imp.plugin.photo.style.index.CircleIndexIndicator;
import com.inspur.imp.plugin.photo.style.progress.ProgressBarIndicator;
import com.inspur.imp.plugin.photo.style.progress.ProgressPieIndicator;
import com.inspur.imp.plugin.photo.transfer.TransferConfig;
import com.inspur.imp.plugin.photo.transfer.TransferLayout;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2018/7/5.
 */

@ContentView(R.layout.plugin_activity_image_gallery)
public class ImageGalleryActivity extends BaseActivity {

    @ViewInject(R.id.rl_content)
    private RelativeLayout contentLayout;


    private TransferLayout transLayout;
    private TransferConfig transConfig;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏

        List<String> thumbnailImageList = new ArrayList<>();
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486263782969.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1485055822651.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486194909983.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486194996586.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486195059137.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486173497249.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486173526402.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486173639603.png@233w_160h_20q");
        thumbnailImageList.add("http://static.fdc.com.cn/avatar/sns/1486172566083.png@233w_160h_20q");

        List<String> sourceImageList = new ArrayList<>();
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486263782969.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1485055822651.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486194909983.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486194996586.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486195059137.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486173497249.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486173526402.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486173639603.png");
        sourceImageList.add("http://static.fdc.com.cn/avatar/sns/1486172566083.png");
        transLayout = new TransferLayout(this);
        contentLayout.addView(transLayout);
        transConfig= TransferConfig.build()
                .setThumbnailImageList(thumbnailImageList)
                .setSourceImageList(sourceImageList)
                .setMissPlaceHolder(R.drawable.plugin_camera_no_pictures)
                .setErrorPlaceHolder(R.drawable.plugin_camera_no_pictures)
                .setProgressIndicator(new ProgressPieIndicator())
                .setIndexIndicator(new CircleIndexIndicator())
                .setJustLoadHitImage(false)
                .setNowThumbnailIndex(0)
                .setDuration(0)
                .create();
        checkConfig();
        transLayout.apply(transConfig);
        transLayout.show();
    }
    /**
     * 检查参数，如果必须参数缺少，就使用缺省参数或者抛出异常
     */
    private void checkConfig() {
        if (transConfig.isSourceEmpty())
            throw new IllegalArgumentException("the parameter sourceImageList can't be empty");

        transConfig.setNowThumbnailIndex(transConfig.getNowThumbnailIndex() < 0
                ? 0 : transConfig.getNowThumbnailIndex());

        transConfig.setOffscreenPageLimit(transConfig.getOffscreenPageLimit() <= 0
                ? 1 : transConfig.getOffscreenPageLimit());

        transConfig.setDuration(transConfig.getDuration() <= 0
                ? 300 : transConfig.getDuration());

        transConfig.setProgressIndicator(transConfig.getProgressIndicator() == null
                ? new ProgressBarIndicator() : transConfig.getProgressIndicator());

        transConfig.setIndexIndicator(transConfig.getIndexIndicator() == null
                ? new CircleIndexIndicator() : transConfig.getIndexIndicator());

        transConfig.setImageLoader(transConfig.getImageLoader() == null
                ? UniversalImageLoader.with(getApplicationContext())
                : transConfig.getImageLoader());
    }


}
