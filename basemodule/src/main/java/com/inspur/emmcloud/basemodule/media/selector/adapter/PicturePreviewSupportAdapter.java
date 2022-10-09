package com.inspur.emmcloud.basemodule.media.selector.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.player.view.VideoPlayerView;
import com.inspur.emmcloud.basemodule.media.selector.adapter.holder.BasePreviewHolder;
import com.inspur.emmcloud.basemodule.media.selector.adapter.holder.PreviewSupportHolder;
import com.inspur.emmcloud.basemodule.media.selector.basic.IPagerAdapterLifecycle;
import com.inspur.emmcloud.basemodule.media.selector.config.PictureMimeType;
import com.inspur.emmcloud.basemodule.media.selector.config.PictureSelectionConfig;
import com.inspur.emmcloud.basemodule.media.selector.entity.LocalMedia;
import com.inspur.emmcloud.basemodule.media.selector.interfaces.OnCallbackListener;
import com.inspur.emmcloud.basemodule.media.selector.photoview.OnViewTapListener;
import com.inspur.emmcloud.basemodule.media.selector.photoview.PhotoView;
import com.inspur.emmcloud.basemodule.media.selector.utils.BitmapUtils;
import com.inspur.emmcloud.basemodule.media.selector.utils.DensityUtil;
import com.inspur.emmcloud.basemodule.media.selector.utils.MediaUtils;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Date：2022/6/29
 * Author：wang zhen
 * Description support版本使用viewpager adapter
 */
public class PicturePreviewSupportAdapter extends PagerAdapter implements IPagerAdapterLifecycle {
    private List<LocalMedia> mData;
    private Context context;
    private int screenWidth;
    private int screenHeight;
    private int screenAppInHeight;
    private LocalMedia media;
    private PictureSelectionConfig config;
    private BasePreviewHolder.OnPreviewEventListener mPreviewEventListener;
    // 视频播放，销毁时释放资源
    private final LinkedHashMap<Integer, PreviewSupportHolder> mItemCache = new LinkedHashMap<>();
    // 当前展示的界面索引
    private int currentSelectedItem = 0;


    // 构造
    public PicturePreviewSupportAdapter(Context context, List<LocalMedia> list) {
        this.mData = list;
        this.context = context;
    }

    // 设置事件监听
    public void setOnPreviewEventListener(BasePreviewHolder.OnPreviewEventListener listener) {
        this.mPreviewEventListener = listener;
    }

    @Override
    public int getCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {
        View contentView = LayoutInflater.from(container.getContext())
                .inflate(R.layout.ps_preview_item, container, false);
        PhotoView photoView = (PhotoView) contentView.findViewById(R.id.preview_image);
        ImageView playIv = (ImageView) contentView.findViewById(R.id.iv_play_video);
        VideoPlayerView playerView = (VideoPlayerView) contentView.findViewById(R.id.video_player_view);
        View surface = (View) contentView.findViewById(R.id.surface_click_view);
        // 基本参数
        this.config = PictureSelectionConfig.getInstance();
        this.screenWidth = DensityUtil.getRealScreenWidth(contentView.getContext());
        this.screenHeight = DensityUtil.getScreenHeight(contentView.getContext());
        this.screenAppInHeight = DensityUtil.getRealScreenHeight(contentView.getContext());
        final LocalMedia media = getMediaItem(position);
        int[] size = getSize(media);
        int[] maxImageSize = BitmapUtils.getMaxImageSize(size[0], size[1]);
        loadImageBitmap(photoView, media, maxImageSize[0], maxImageSize[1]);
        setScaleDisplaySize(media, photoView);
        // 图片点击事件，仿微信
        photoView.setOnViewTapListener(new OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                if (mPreviewEventListener != null) {
                    mPreviewEventListener.onBackPressed();
                }
            }
        });

        // 仿微信时使用不到
        photoView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mPreviewEventListener != null) {
                    mPreviewEventListener.onLongPressDownload(media);
                }
                return false;
            }
        });
        boolean hasVideo = PictureMimeType.isHasVideo(media.getMimeType());
        PreviewSupportHolder previewHolder = new PreviewSupportHolder(photoView, playIv, hasVideo ? playerView : null, !hasVideo,  !hasVideo || PictureMimeType.isMP4(media.getMimeType()));
        if (hasVideo) {
            // 视频
            playIv.setVisibility(View.VISIBLE);
            playIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // 播放视频流
                        (mItemCache.get(position)).startVideo(media);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            });
            // 视频、图片点击事件，统一处理
            surface.setVisibility(View.VISIBLE);
            surface.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPreviewEventListener != null) {
                        mPreviewEventListener.onBackPressed();
                    }
                }
            });
        } else {
            // 图片
            surface.setVisibility(View.GONE);
            playIv.setVisibility(View.GONE);
        }
        // 缓存holder，销毁时释放资源
        mItemCache.put(position, previewHolder);
        attach(position);
        // 添加itemView
        (container).addView(contentView, 0);
        return contentView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        // 界面解绑，释放播放器资源
        detach(position);
        (container).removeView((View) object);
    }

    // 获取当前preview holder
    public PreviewSupportHolder getCurrentHolder(int position) {
        if (mItemCache.isEmpty()) return null;
        return mItemCache.get(position);
    }

    public LocalMedia getMediaItem(int position) {
        if (position > mData.size()) {
            return null;
        }
        return mData.get(position);
    }

    private int[] getSize(LocalMedia media) {
        if (media.isCut() && media.getCropImageWidth() > 0 && media.getCropImageHeight() > 0) {
            return new int[]{media.getCropImageWidth(), media.getCropImageHeight()};
        } else {
            return new int[]{media.getWidth(), media.getHeight()};
        }
    }

    protected void setScaleDisplaySize(LocalMedia media, final PhotoView photoView) {
        if (!config.isPreviewZoomEffect && screenWidth < screenHeight) {
            if (media.getWidth() > 0 && media.getHeight() > 0) {
                float ratio = (float) media.getWidth() / (float) media.getHeight();
                int displayHeight = (int) (screenWidth / ratio);
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) photoView.getLayoutParams();
                layoutParams.width = screenWidth;
                layoutParams.height = displayHeight > screenHeight ? screenAppInHeight : screenHeight;
                layoutParams.gravity = Gravity.CENTER;
            }
        }
    }

    protected void loadImageBitmap(final PhotoView photoView, final LocalMedia media, int maxWidth, int maxHeight) {
        if (PictureSelectionConfig.imageEngine != null) {
            PictureSelectionConfig.imageEngine.loadImageBitmap(context, media.getAvailablePath(), maxWidth, maxHeight,
                    new OnCallbackListener<Bitmap>() {
                        @Override
                        public void onCall(Bitmap bitmap) {
                            loadBitmapCallback(media, bitmap, photoView);
                        }
                    });
        }
    }

    protected void loadBitmapCallback(LocalMedia media, Bitmap bitmap, final PhotoView photoView) {
        String path = media.getAvailablePath();
        if (bitmap == null) {
            mPreviewEventListener.onLoadError();
        } else {
            if (PictureMimeType.isHasWebp(media.getMimeType()) || PictureMimeType.isUrlHasWebp(path)
                    || PictureMimeType.isUrlHasGif(path) || PictureMimeType.isHasGif(media.getMimeType())) {
                if (PictureSelectionConfig.imageEngine != null) {
                    photoView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    PictureSelectionConfig.imageEngine.loadImage(context, path, photoView);
                }
            } else {
                setImageViewBitmap(bitmap, photoView);
            }
            if (media.getWidth() <= 0) {
                media.setWidth(bitmap.getWidth());
            }
            if (media.getHeight() <= 0) {
                media.setHeight(bitmap.getHeight());
            }
            int width, height;
            final ImageView.ScaleType scaleType;
            if (MediaUtils.isLongImage(bitmap.getWidth(), bitmap.getHeight())) {
                scaleType = ImageView.ScaleType.CENTER_CROP;
                width = screenWidth;
                height = screenHeight;
            } else {
                scaleType = ImageView.ScaleType.FIT_CENTER;
                int[] size = getSize(media);
                boolean isHaveSize = bitmap.getWidth() > 0 && bitmap.getHeight() > 0;
                width = isHaveSize ? bitmap.getWidth() : size[0];
                height = isHaveSize ? bitmap.getHeight() : size[1];
            }
            mPreviewEventListener.onLoadComplete(width, height, new OnCallbackListener<Boolean>() {
                @Override
                public void onCall(Boolean isBeginEffect) {
                    photoView.setScaleType(isBeginEffect ? ImageView.ScaleType.CENTER_CROP : scaleType);
                }
            });
        }
    }

    private void setImageViewBitmap(Bitmap bitmap, PhotoView photoView) {
        photoView.setImageBitmap(bitmap);
    }

    public int getCurrentSelectedItem() {
        return currentSelectedItem;
    }

    public void setCurrentSelectedItem(int currentSelectedItem) {
        this.currentSelectedItem = currentSelectedItem;
    }

    public PreviewSupportHolder getCurrentSelectedHolder(){
        return getCurrentHolder(currentSelectedItem);
    }
    /**
     * 释放当前视频相关
     */
    public void destroy() {
        for (Integer key : mItemCache.keySet()) {
            PreviewSupportHolder holder = mItemCache.get(key);
            if (!holder.isPicType) {
                holder.releaseVideo();
            }
        }
    }

    // 修复编辑图片完成刷新失败问题
    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    // item绑定到adapter
    @Override
    public void attach(int currentPosition) {
        try {
            (mItemCache.get(currentPosition)).attachVideo();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    // fragment生命周期onResume
    @Override
    public void resume(int currentPosition) {
        // 继续之后不会恢复播放，需要点击从头播放,不做继续播放的处理
    }

    // fragment生命周期onPause
    @Override
    public void pause(int currentPosition) {
        try {
            (mItemCache.get(currentPosition)).pauseVideo();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    // item从adapter解绑
    @Override
    public void detach(int currentPosition) {
        try {
            (mItemCache.get(currentPosition)).detachVideo();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
