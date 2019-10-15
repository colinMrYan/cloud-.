package com.inspur.emmcloud.ui.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.EventMessage;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.widget.largeimage.LargeImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.NonViewAware;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import uk.co.senab.photoview.PhotoViewAttacher;

import static android.R.attr.path;


/**
 * 单张图片显示Fragment
 */
public class ImageDetailFragment extends Fragment {
    private String mImageUrl;
    private LargeImageView mImageView;
    private ProgressBar progressBar;
    private PhotoViewAttacher mAttacher;

    private int rawImageHigh = 0;
    private int rawImageWide = 0;
    private int previewHigh = 0;
    private int previewWide = 0;
    private String rawUrl = null;

    private int locationW, locationH, locationX, locationY;
    private boolean isNeedTransformOut;
    private boolean isNeedTransformIn;
    private DownLoadProgressRefreshListener downLoadProgressRefreshListener;
    private ImageLoadingProgressListener imageLoadingProgressListener;


    public static ImageDetailFragment newInstance(String imageUrl, int w, int h, int x, int y, boolean isNeedTransformIn, boolean isNeedTransformOut, int preViewH, int preViewW, int rawH, int rawW) {
        final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString("url", imageUrl);
        args.putInt("w", w);
        args.putInt("h", h);
        args.putInt("x", x);
        args.putInt("y", y);
        args.putInt("rawH", rawH);
        args.putInt("rawW", rawW);
        args.putInt("preH", preViewH);
        args.putInt("preW", preViewW);
        args.putBoolean("isNeedTransformOut", isNeedTransformOut);
        args.putBoolean("isNeedTransformIn", isNeedTransformIn);
        f.setArguments(args);
        return f;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString("url")
                : null;
        locationH = getArguments() != null ? getArguments().getInt("h")
                : null;
        locationW = getArguments() != null ? getArguments().getInt("w")
                : null;
        locationX = getArguments() != null ? getArguments().getInt("x")
                : null;
        locationY = getArguments() != null ? getArguments().getInt("y")
                : null;
        rawImageHigh = getArguments() != null ? getArguments().getInt("rawH")
                : 0;
        rawImageWide = getArguments() != null ? getArguments().getInt("rawW")
                : 0;
        previewHigh = getArguments() != null ? getArguments().getInt("preH")
                : 0;
        previewWide = getArguments() != null ? getArguments().getInt("preW")
                : 0;
        isNeedTransformOut = getArguments() != null && getArguments().getBoolean("isNeedTransformOut");
        isNeedTransformIn = getArguments() != null && getArguments().getBoolean("isNeedTransformIn");

        rawUrl = mImageUrl;

        boolean isHaveOriginalImageCatch = ImageDisplayUtils.getInstance().isHaveCacheImage(rawUrl);//这个是判断有无原图（是否有）

        if (previewHigh != 0
                && ((rawImageHigh != previewHigh) || (rawImageWide != previewWide))
                && !isHaveOriginalImageCatch) {
            rawUrl = mImageUrl;
            mImageUrl = mImageUrl + "&resize=true&w=" + previewWide + "&h=" + previewHigh;
        }

        imageLoadingProgressListener = new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                downLoadProgressRefreshListener.refreshProgress(imageUri, (current * 100) / total);
            }
        };
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_image_pager_detail,
                container, false);

        mImageView = v.findViewById(R.id.image);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventMessage eventMessage = new EventMessage("", Constant.EVENTBUS_TAG_ON_PHOTO_TAB);
                EventBus.getDefault().post(eventMessage);
            }
        });
        mImageView.setOnFlingDownLister(new LargeImageView.OnFlingDownListe() {
            @Override
            public void onFlingDown() {
                closeImg();
            }
        });
        mImageView.setCriticalScaleValueHook(new LargeImageView.CriticalScaleValueHook() {
            @Override
            public float getMinScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMinScale) {
                return 1;
            }

            @Override
            public float getMaxScale(LargeImageView largeImageView, int imageWidth, int imageHeight, float suggestMaxScale) {
                return 5;
            }
        });
        progressBar = v.findViewById(R.id.loading);
        return v;
    }


    /**
     * 关闭图片显示
     */
    public void closeImg() {
        EventMessage eventMessage = new EventMessage("", Constant.EVENTBUS_TAG_ON_PHOTO_CLOSE);
        EventBus.getDefault().post(eventMessage);
        InputMethodUtils.hide(getActivity());
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    /**
     * 保存图片
     * 从保存ImageView缓存改为从网络下载 无网络给出提示  190626
     */
    public void downloadImg(DownLoadProgressRefreshListener downLoadProgressRefreshListener) {
        this.downLoadProgressRefreshListener = downLoadProgressRefreshListener;
        String url = StringUtils.isBlank(rawUrl) ? mImageUrl : rawUrl;
        if (ImageDisplayUtils.getInstance().isHaveCacheImage(url)) {
            File imageFileCatch = DiskCacheUtils.findInCache(url, ImageLoader.getInstance().getDiskCache());
            Bitmap bitmap = BitmapFactory.decodeFile(imageFileCatch.getPath());
            saveBitmapToLocalFromImageLoader(bitmap);
        } else {
            loadingOriginalPicture(true);
        }
    }

    /**
     * 保存并显示把图片展示出来
     *
     * @param context
     * @param cameraPath
     */
    private void refreshGallery(Context context, String cameraPath) {
        File file = new File(cameraPath);
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    file.getAbsolutePath(), file.getName(), null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));
    }

    /**
     * 保存图片
     *
     * @param bitmap
     */
    public String saveBitmapFile(Bitmap bitmap) {
        File temp = new File("/sdcard/IMP-Cloud/cache/chat/");// 要保存文件先创建文件夹
        if (!temp.exists()) {
            temp.mkdir();
        }
        // 重复保存时，覆盖原同名图片
        // 将要保存图片的路径和图片名称
        String savedImagePath = "";
        File file = new File("/sdcard/IMP-Cloud/cache/chat/"
                + FileUtils.getFileName(mImageUrl));
        savedImagePath = "/sdcard/IMP-Cloud/cache/chat/"
                + FileUtils.getFileName(mImageUrl);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            bos.flush();
            bos.close();
            ToastUtils.show(BaseApplication.getInstance(), BaseApplication.getInstance().getString(R.string.save_success));
        } catch (IOException e) {
            ToastUtils.show(BaseApplication.getInstance(), BaseApplication.getInstance().getString(R.string.save_fail));
            e.printStackTrace();
        }
        return savedImagePath;
    }


    @Override
    public void onStart() {
        super.onStart();
        showImageResouce();

    }

    private void showImageResouce() {
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_image)
                .showImageOnFail(R.drawable.default_image)
                .showImageOnLoading(R.drawable.default_image)
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

        if (!StringUtils.isBlank(rawUrl) && ImageDisplayUtils.getInstance().isHaveCacheImage(rawUrl)) {
            String path = ImageDisplayUtils.getInstance().getCacheImageFile(rawUrl).getAbsolutePath();
            try {
                FileInputStream fis = new FileInputStream(path);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                mImageView.setImage(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                mImageView.setImage(R.drawable.default_image);
            }
        } else {
            ImageLoader.getInstance().loadImage(mImageUrl, options,
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            if (getActivity() != null) {
                                mImageView.setImage(R.drawable.default_image);
                                progressBar.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view,
                                                    FailReason failReason) {
                            if (getActivity() != null) {
                                mImageView.setImage(R.drawable.default_image);
                                progressBar.setVisibility(View.GONE);
                            }
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view,
                                                      Bitmap loadedImage) {
                            if (getActivity() != null) {
                                progressBar.setVisibility(View.GONE);
                                mImageView.setImage(loadedImage);
                            }
                        }
                    });
        }
    }

    @Override
    public void onDestroy() {
        if (!StringUtils.isBlank(rawUrl)) {
            ImageSize imageSize = new ImageSize(rawImageWide,
                    rawImageHigh);
            NonViewAware imageAware = new NonViewAware(rawUrl, imageSize, ViewScaleType.CROP);
            ImageLoader.getInstance().cancelDisplayTask(imageAware);
        }
        super.onDestroy();
    }

    /**
     * 加载图片
     */
    public void loadingImage(DownLoadProgressRefreshListener downLoadProgressRefreshListener) {
        this.downLoadProgressRefreshListener = downLoadProgressRefreshListener;
        loadingOriginalPicture(false);
    }


    /**
     * ImageView 加载图片
     */
    private void loadingOriginalPicture(final boolean isSaveImage2Local) {
        String url = rawUrl == null ? mImageUrl : rawUrl;
        LogUtils.LbcDebug("获取到的Url:::" + url);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_image)
                .showImageOnFail(R.drawable.default_image)
                .showImageOnLoading(R.drawable.default_image)
                .imageScaleType(ImageScaleType.NONE)
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageSize imageSize = new ImageSize(rawImageWide,
                rawImageHigh);
        ImageLoader.getInstance().loadImage(url, imageSize,
                options, new ImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        LogUtils.jasonDebug("onLoadingStarted");
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                        LogUtils.jasonDebug("onLoadingFailed");
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        downLoadProgressRefreshListener.loadingComplete(imageUri);
                        LogUtils.LbcDebug("下载完成图片更新" + imageUri);
                        if (getActivity() != null) {
                            showImageResouce();
                            if (isSaveImage2Local) {
                                saveBitmapToLocalFromImageLoader(loadedImage);
                            }
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        LogUtils.jasonDebug("onLoadingCancelled");
                    }
                }, imageLoadingProgressListener);

    }


    /**
     * 保存
     */
    private void saveBitmapToLocalFromImageLoader(Bitmap bitmap) {
        String savedImagePath = saveBitmapFile(bitmap);
        refreshGallery(BaseApplication.getInstance(), savedImagePath);
    }

    public interface DownLoadProgressRefreshListener {
        public void refreshProgress(String url, int progress);

        public void loadingComplete(String url);
    }

}
