package com.inspur.emmcloud.web.plugin.photo;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.gyf.barlibrary.ImmersionBar;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.MyAppConfig;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.DownLoaderUtils;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.R2;
import com.inspur.emmcloud.web.plugin.photo.loader.UniversalImageLoader;
import com.inspur.emmcloud.web.plugin.photo.style.index.CircleIndexIndicator;
import com.inspur.emmcloud.web.plugin.photo.style.progress.ProgressBarIndicator;
import com.inspur.emmcloud.web.plugin.photo.style.progress.ProgressPieIndicator;
import com.inspur.emmcloud.web.plugin.photo.transfer.TransferConfig;
import com.inspur.emmcloud.web.plugin.photo.transfer.TransferLayout;

import org.xutils.common.Callback;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by chenmch on 2018/7/5.
 */

public class ImageGalleryActivity extends BaseActivity {

    public static final String EXTRA_IMAGE_SOURCE_URLS = "image_source_urls";
    public static final String EXTRA_IMAGE_THUMB_URLS = "image_thumb_urls";
    public static final String EXTRA_IMAGE_INDEX = "image_index";

    @BindView(R2.id.rl_content)
    RelativeLayout contentLayout;

    private TransferLayout transLayout;
    private TransferConfig transConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//设置全屏
        List<String> sourceImageList = getIntent().getStringArrayListExtra(EXTRA_IMAGE_SOURCE_URLS);
        List<String> thumbnailImageList = getIntent().getStringArrayListExtra(EXTRA_IMAGE_THUMB_URLS);
        int index = getIntent().getIntExtra(EXTRA_IMAGE_INDEX, 0);
        transLayout = new TransferLayout(this);
        contentLayout.addView(transLayout);
        transConfig = TransferConfig.build()
                .setThumbnailImageList(thumbnailImageList)
                .setSourceImageList(sourceImageList)
                .setMissPlaceHolder(R.drawable.plugin_camera_no_pictures)
                .setErrorPlaceHolder(R.drawable.plugin_camera_no_pictures)
                .setProgressIndicator(new ProgressPieIndicator())
                .setIndexIndicator(new CircleIndexIndicator())
                .setJustLoadHitImage(false)
                .setNowThumbnailIndex(index)
                .setDuration(0)
                .create();
        checkConfig();
        transLayout.apply(transConfig);
        transLayout.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        ImmersionBar.with(this).navigationBarColor(android.R.color.black).init();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.plugin_activity_image_gallery;
    }

    protected int getStatusType() {
        return STATUS_WHITE;
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

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.iv_close) {
            finish();

        } else if (i == R.id.iv_download) {
            int position = transConfig.getNowThumbnailIndex();
            String url = transConfig.getSourceImageList().get(position);
            saveImg(url);

        }
    }

    private void saveImg(String url) {
        String savePath = MyAppConfig.getFileDownloadDirPath() + System.currentTimeMillis() + ".jpg";
        new DownLoaderUtils().startDownLoad(url, savePath, new Callback.ProgressCallback<File>() {
            @Override
            public void onWaiting() {

            }

            @Override
            public void onStarted() {

            }

            @Override
            public void onLoading(long l, long l1, boolean b) {

            }

            @Override
            public void onSuccess(File file) {

            }

            @Override
            public void onError(Throwable throwable, boolean b) {
                ToastUtils.show(getApplicationContext(), R.string.download_fail);
            }

            @Override
            public void onCancelled(CancelledException e) {
            }

            @Override
            public void onFinished() {
                ToastUtils.show(getApplicationContext(), "图片已保存至IMP-Cloud/download/文件夹");
            }
        });
    }


}
