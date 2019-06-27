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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.FileUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.InputMethodUtils;
import com.inspur.emmcloud.bean.system.EventMessage;
import com.inspur.emmcloud.widget.SmoothImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.DiskCacheUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.BufferedOutputStream;
import java.io.File;
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
    private SmoothImageView mImageView;
    private ProgressBar progressBar;
    private PhotoViewAttacher mAttacher;

    private int rawImageHigh = 0;
    private int rawImageWide = 0;
    private String rawUrl;

    private int locationW, locationH, locationX, locationY;
    private boolean isNeedTransformOut;
    private boolean isNeedTransformIn;
    private DownLoadProgressRefreshListener downLoadProgressRefreshListener;
    private ImageLoadingProgressListener imageLoadingProgressListener;

    public static ImageDetailFragment newInstance(String imageUrl, int w, int h, int x, int y, boolean isNeedTransformIn, boolean isNeedTransformOut) {
        final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString("url", imageUrl);
        args.putInt("w", w);
        args.putInt("h", h);
        args.putInt("x", x);
        args.putInt("y", y);
        args.putBoolean("isNeedTransformOut", isNeedTransformOut);
        args.putBoolean("isNeedTransformIn", isNeedTransformIn);
        f.setArguments(args);
        return f;
    }

    public static ImageDetailFragment newInstance(String imageUrl, int w, int h, int x, int y, boolean isNeedTransformIn, boolean isNeedTransformOut, int rawH, int rawW, String rawUrl) {
        final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString("url", imageUrl);
        args.putString("rawUrl", rawUrl);
        args.putInt("w", w);
        args.putInt("h", h);
        args.putInt("x", x);
        args.putInt("y", y);
        args.putInt("rawH", rawH);
        args.putInt("rawW", rawW);
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
        rawUrl = getArguments() != null ? getArguments().getString("rawUrl")
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
        isNeedTransformOut = getArguments() != null && getArguments().getBoolean("isNeedTransformOut");
        isNeedTransformIn = getArguments() != null && getArguments().getBoolean("isNeedTransformIn");

        imageLoadingProgressListener = new ImageLoadingProgressListener() {
            @Override
            public void onProgressUpdate(String imageUri, View view, int current, int total) {
                LogUtils.LbcDebug("更新大小:::" + current);
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
        mImageView.setOriginalInfo(locationW, locationH, locationX, locationY);
        if (isNeedTransformIn) {
            mImageView.transformIn();
        }
        mImageView.setOnTransformListener(new SmoothImageView.TransformListener() {
            @Override
            public void onTransformComplete(int mode) {
                if (mode == 2) {
                    getActivity().finish();
                    getActivity().overridePendingTransition(0, 0);
                }
            }
        });
        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
            @Override
            public void onPhotoTap(View view, float x, float y) {
                onOutsidePhotoTap();
            }

            @Override
            public void onOutsidePhotoTap() {
                EventMessage eventMessage = new EventMessage("", Constant.EVENTBUS_TAG_ON_PHOTO_TAB);
                EventBus.getDefault().post(eventMessage);
            }
        });
        mAttacher.setOnSingleFlingListener(new PhotoViewAttacher.OnSingleFlingListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (Math.abs(velocityY) > Math.abs(velocityX) && e2.getY() - e1.getY() > DensityUtil.dip2px(getContext(), 30)) {
                    closeImg();
                }
                return false;
            }
        });
        mAttacher.setMaximumScale(5);
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
        if (isNeedTransformOut && locationW != 0) {
            mImageView.transformOut();
        } else {
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    /**
     * 保存图片
     * 从保存ImageView缓存改为从网络下载 无网络给出提示  190626
     */
    public void downloadImg(DownLoadProgressRefreshListener downLoadProgressRefreshListener) {
        this.downLoadProgressRefreshListener = downLoadProgressRefreshListener;
        String url = StringUtils.isBlank(rawUrl) ? mImageUrl : rawUrl;
        if (ImageDisplayUtils.getInstance().isHaveImage(url)) {
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
            ToastUtils.show(getActivity(), getString(R.string.save_success));
        } catch (IOException e) {
            ToastUtils.show(getActivity(), getString(R.string.save_fail));
            e.printStackTrace();
        }
        return savedImagePath;
    }


    @Override
    public void onStart() {
        super.onStart();
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_image)

                .showImageOnFail(R.drawable.default_image)
                .showImageOnLoading(R.drawable.default_image)
                // 设置图片的解码类型
                .bitmapConfig(Bitmap.Config.RGB_565)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoader.getInstance().displayImage(mImageUrl, mImageView, options,
                new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        if (getActivity() != null) {
                            progressBar.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view,
                                                FailReason failReason) {
                        if (getActivity() != null) {
                            String message = null;
                            switch (failReason.getType()) {
                                case IO_ERROR:
                                    message = getString(R.string.download_fail);
                                    break;
                                case DECODING_ERROR:
                                    message = getString(R.string.picture_cannot_show);
                                    break;
                                case NETWORK_DENIED:
                                    message = getString(R.string.cannot_download_for_network_exception);
                                    break;
                                case OUT_OF_MEMORY:
                                    message = getString(R.string.cannot_show_for_too_big);
                                    break;
                                case UNKNOWN:
                                    message = getString(R.string.unknown_error);
                                    break;
                                default:
                                    message = getString(R.string.download_fail);
                                    break;
                            }
//							Toast.makeText(getActivity(), message,
//									Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view,
                                                  Bitmap loadedImage) {
                        if (getActivity() != null) {
                            progressBar.setVisibility(View.GONE);
                            mAttacher.update();
                        }
                    }
                });
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
    private void loadingOriginalPicture(boolean isSaveImage2Local) {
        String url = rawUrl == null ? mImageUrl : rawUrl;
        LogUtils.LbcDebug("获取到的Url:::" + url);
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.default_image)
                .showImageOnFail(R.drawable.default_image)
                .showImageOnLoading(R.drawable.default_image)
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

                    }

                    @Override
                    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        downLoadProgressRefreshListener.loadingComplete(imageUri);
                        LogUtils.LbcDebug("下载完成图片更新" + imageUri);
                        DisplayImageOptions options = new DisplayImageOptions.Builder()
                                .showImageForEmptyUri(R.drawable.default_image)

                                .showImageOnFail(R.drawable.default_image)
                                .showImageOnLoading(R.drawable.default_image)
                                // 设置图片的解码类型
                                .bitmapConfig(Bitmap.Config.RGB_565)
                                .cacheInMemory(true)
                                .cacheOnDisk(true)
                                .build();
                        ImageLoader.getInstance().displayImage(imageUri, mImageView, options);
                        if (isSaveImage2Local) {
                            saveBitmapToLocalFromImageLoader(loadedImage);
                        }
                    }

                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {

                    }
                }, imageLoadingProgressListener);

    }


    /**
     * 保存
     */
    private void saveBitmapToLocalFromImageLoader(Bitmap bitmap) {
        String savedImagePath = saveBitmapFile(bitmap);
        refreshGallery(getActivity(), savedImagePath);
    }

    public interface DownLoadProgressRefreshListener {
        public void refreshProgress(String url, int progress);

        public void loadingComplete(String url);
    }

}
