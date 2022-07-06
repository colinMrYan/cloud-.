package com.inspur.emmcloud.util.privates;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.ContextCompat;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.media.luban.Luban;
import com.inspur.emmcloud.basemodule.media.luban.OnNewCompressListener;
import com.inspur.emmcloud.basemodule.media.luban.OnRenameListener;
import com.inspur.emmcloud.basemodule.media.selector.animators.AnimationType;
import com.inspur.emmcloud.basemodule.media.selector.basic.PictureSelectionModel;
import com.inspur.emmcloud.basemodule.media.selector.basic.PictureSelector;
import com.inspur.emmcloud.basemodule.media.selector.config.PictureSelectionConfig;
import com.inspur.emmcloud.basemodule.media.selector.config.SelectLimitType;
import com.inspur.emmcloud.basemodule.media.selector.config.SelectModeConfig;
import com.inspur.emmcloud.basemodule.media.selector.demo.GlideEngine;
import com.inspur.emmcloud.basemodule.media.selector.engine.CompressFileEngine;
import com.inspur.emmcloud.basemodule.media.selector.engine.UriToFileTransformEngine;
import com.inspur.emmcloud.basemodule.media.selector.interfaces.OnKeyValueResultCallbackListener;
import com.inspur.emmcloud.basemodule.media.selector.interfaces.OnSelectLimitTipsListener;
import com.inspur.emmcloud.basemodule.media.selector.language.LanguageConfig;
import com.inspur.emmcloud.basemodule.media.selector.style.BottomNavBarStyle;
import com.inspur.emmcloud.basemodule.media.selector.style.PictureSelectorStyle;
import com.inspur.emmcloud.basemodule.media.selector.style.SelectMainStyle;
import com.inspur.emmcloud.basemodule.media.selector.style.TitleBarStyle;
import com.inspur.emmcloud.basemodule.media.selector.utils.DateUtils;
import com.inspur.emmcloud.basemodule.media.selector.utils.SandboxTransformUtils;


import java.io.File;
import java.util.ArrayList;

public class PictureSelectorUtils {
    private static PictureSelectorUtils mInstance;
    private static final int GELLARY_RESULT = 2;

    public static PictureSelectorUtils getInstance() {
        if (mInstance == null) {
            synchronized (PictureSelectorUtils.class) {
                if (mInstance == null) {
                    mInstance = new PictureSelectorUtils();
                }
            }
        }
        return mInstance;
    }


    public void openGalleryInDefault(Context context) {
        PictureSelectorStyle selectorStyle = new PictureSelectorStyle();
        selectorStyle.setSelectMainStyle(getMainSelectorStyle(context));
        selectorStyle.setBottomBarStyle(getBottomNavBarStyle(context));
        selectorStyle.setTitleBarStyle(getTitleBarStyle());
        PictureSelectionModel selectionModel = getSelectionModel(context, SelectModeConfig.MULTIPLE, true, true, false, 5, 5);
        selectionModel.setSelectorUIStyle(selectorStyle);
        selectionModel.forResult(GELLARY_RESULT);
    }

    public void openGallery(Context context, int chooseMode, boolean isDisplayTimeAxis, boolean useOriginalControl, boolean displayCamera, int maxImageNumber, int maxVideoNumber) {
        PictureSelectorStyle selectorStyle = new PictureSelectorStyle();
        selectorStyle.setSelectMainStyle(getMainSelectorStyle(context));
        selectorStyle.setBottomBarStyle(getBottomNavBarStyle(context));
        selectorStyle.setTitleBarStyle(getTitleBarStyle());
        PictureSelectionModel selectionModel = getSelectionModel(context, chooseMode, isDisplayTimeAxis, useOriginalControl, displayCamera, maxImageNumber, maxVideoNumber);
        selectionModel.setSelectorUIStyle(selectorStyle);
        selectionModel.forResult(GELLARY_RESULT);
    }

    private PictureSelectionModel getSelectionModel(Context context, int chooseMode, boolean isDisplayTimeAxis, boolean useOriginalControl, boolean displayCamera, int maxImageNumber, int maxVideoNumber) {
        PictureSelectionModel selectionModel = PictureSelector.create(context)
                // 目前只支持图片，后续完善视频
                .openGallery(1)
                // 微信样式
//                .setSelectorUIStyle(selectorStyle)
                .setImageEngine(GlideEngine.createGlideEngine())
                // 剪裁引擎 暂时不用
//              .setCropEngine(getCropFileEngine())

                .setCompressEngine(new ImageFileCompressEngine())
                .setSandboxFileEngine(new MeSandboxFileEngine())
                // 相机相关 暂时不用
//                                .setCameraInterceptListener(getCustomCameraEvent())
                // 录音相关 暂时不用
//                                .setRecordAudioInterceptListener(new MeOnRecordAudioInterceptListener())
                .setSelectLimitTipsListener(new MeOnSelectLimitTipsListener())
                // 编辑 暂时不用
//                                .setEditMediaInterceptListener(getCustomEditMediaEvent())
//                .setPermissionDescriptionListener(getPermissionDescriptionListener())
                // 自定义预览，不用
//                .setPreviewInterceptListener(getPreviewInterceptListener())
//                                .setPermissionDeniedListener(getPermissionDeniedListener())
                // 添加水印，暂时不用
//                .setAddBitmapWatermarkListener(getAddBitmapWatermarkListener())
                // 视频缩略图，暂时不用
//                .setVideoThumbnailListener(getVideoThumbnailEventListener())
                // 视频自动播放，不需要
//                .isAutoVideoPlay(cb_auto_video.isChecked())
                // 视频循环播放，不需要
//                .isLoopAutoVideoPlay(cb_auto_video.isChecked())
                // 过滤文件类型
//                              .setQueryFilterListener(new OnQueryFilterListener() {
//                                    @Override
//                                    public boolean onFilter(String absolutePath) {
//                                        return PictureMimeType.isUrlHasVideo(absolutePath);
//                                    }
//                                })
                // 自定义加载器
//                                .setExtendLoaderEngine(getExtendLoaderEngine())
                // 自定义布局，不需要
                .setInjectLayoutResourceListener(null)
                .setSelectionMode(SelectModeConfig.MULTIPLE)
                .setLanguage(LanguageConfig.SYSTEM_LANGUAGE)
                // 显示顺序，默认即可
//                .setQuerySortOrder(MediaStore.MediaColumns.DATE_MODIFIED)
                // 时间轴
                .isDisplayTimeAxis(true)
                // 查询指定目录
                .isOnlyObtainSandboxDir(false)
                // 分页模式
                .isPageStrategy(false)
                // 原图功能
                .isOriginalControl(true)
                // 显示相机
                .isDisplayCamera(false)
                // 开启点击声音
                .isOpenClickSound(false)
                // 剪裁默认不支持gif，webp
                .setSkipCropMimeType((String) null)
                // 滑动选择，长按选择，可保留，微信没有
                .isFastSlidingSelect(true)
                //.setOutputCameraImageFileName("luck.jpeg")
                //.setOutputCameraVideoFileName("luck.mp4")
                // 视频图片同选
                .isWithSelectVideoImage(true)
                // 全屏预览，与微信相同
                .isPreviewFullScreenMode(true)
                // 预览缩放效果
                .isPreviewZoomEffect(true)
                // 点击预览 与微信相同
                .isPreviewImage(true)
                // 预览视频，与微信相同
                .isPreviewVideo(true)
                // 音频不需要
                .isPreviewAudio(false)
                //.setQueryOnlyMimeType(PictureMimeType.ofGIF())
                // 是否显示蒙层 false 与微信保持一致
                .isMaxSelectEnabledMask(false)
                // 单选模式直接返回
//                .isDirectReturnSingle()
                // 图片，视频最多选择个数
                .setMaxSelectNum(5)
                .setMaxVideoSelectNum(5)
                // 图片列表加载动画
                .setRecyclerAnimationMode(AnimationType.DEFAULT_ANIMATION)
                // 是否显示gif，默认不显示
                .isGif(false)
                .setSelectedData(null);
        return selectionModel;
    }

    public SelectMainStyle getMainSelectorStyle(Context context) {
        // 主体风格
        SelectMainStyle numberSelectMainStyle = new SelectMainStyle();
        numberSelectMainStyle.setSelectNumberStyle(true);
        numberSelectMainStyle.setPreviewSelectNumberStyle(false);
        numberSelectMainStyle.setPreviewDisplaySelectGallery(true);
        numberSelectMainStyle.setSelectBackground(R.drawable.ps_default_num_selector);
        numberSelectMainStyle.setPreviewSelectBackground(R.drawable.ps_preview_checkbox_selector);
        numberSelectMainStyle.setSelectNormalBackgroundResources(R.drawable.ps_select_complete_normal_bg);
        numberSelectMainStyle.setSelectNormalTextColor(ContextCompat.getColor(context, R.color.ps_color_53575e));
        numberSelectMainStyle.setSelectNormalText(context.getResources().getString(R.string.ps_send));
        numberSelectMainStyle.setAdapterPreviewGalleryBackgroundResource(R.drawable.ps_preview_gallery_bg);
        numberSelectMainStyle.setAdapterPreviewGalleryItemSize(DensityUtil.dip2px(context, 52));
        numberSelectMainStyle.setPreviewSelectText(context.getResources().getString(R.string.ps_select));
        numberSelectMainStyle.setPreviewSelectTextSize(16);
        numberSelectMainStyle.setPreviewSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_white));
        numberSelectMainStyle.setPreviewSelectMarginRight(DensityUtil.dip2px(context, 6));
        numberSelectMainStyle.setSelectBackgroundResources(R.drawable.ps_select_complete_bg);
        numberSelectMainStyle.setSelectText(context.getResources().getString(R.string.ps_send_num));
        numberSelectMainStyle.setSelectTextSize(16);
        numberSelectMainStyle.setSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_white));
        numberSelectMainStyle.setMainListBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_black));
        numberSelectMainStyle.setCompleteSelectRelativeTop(true);
        numberSelectMainStyle.setPreviewSelectRelativeBottom(true);
        numberSelectMainStyle.setAdapterItemIncludeEdge(false);
        return numberSelectMainStyle;
    }

    public TitleBarStyle getTitleBarStyle() {
        // 头部TitleBar 风格
        TitleBarStyle numberTitleBarStyle = new TitleBarStyle();
        numberTitleBarStyle.setHideCancelButton(true);
        numberTitleBarStyle.setAlbumTitleRelativeLeft(true);
        numberTitleBarStyle.setTitleAlbumBackgroundResource(R.drawable.ps_album_bg);
        numberTitleBarStyle.setTitleTextSize(16);
        numberTitleBarStyle.setTitleDrawableRightResource(R.drawable.ps_ic_grey_arrow);
        numberTitleBarStyle.setPreviewTitleLeftBackResource(R.drawable.ps_ic_normal_back);
        return numberTitleBarStyle;
    }

    public BottomNavBarStyle getBottomNavBarStyle(Context context) {
        // 底部NavBar 风格
        BottomNavBarStyle numberBottomNavBarStyle = new BottomNavBarStyle();
        numberBottomNavBarStyle.setBottomPreviewNarBarBackgroundColor(ContextCompat.getColor(context, R.color.ps_color_half_grey));
        numberBottomNavBarStyle.setBottomPreviewNormalText(context.getResources().getString(R.string.ps_preview));
        numberBottomNavBarStyle.setBottomPreviewNormalTextColor(ContextCompat.getColor(context, R.color.ps_color_9b));
        numberBottomNavBarStyle.setBottomPreviewNormalTextSize(16);
        numberBottomNavBarStyle.setCompleteCountTips(false);
        numberBottomNavBarStyle.setBottomPreviewSelectText(context.getResources().getString(R.string.ps_preview_num));
        numberBottomNavBarStyle.setBottomPreviewSelectTextColor(ContextCompat.getColor(context, R.color.ps_color_white));
        numberBottomNavBarStyle.setBottomOriginalTextSize(16);
        return numberBottomNavBarStyle;
    }

    /**
     * 自定义压缩
     */
    private static class ImageFileCompressEngine implements CompressFileEngine {

        @Override
        public void onStartCompress(Context context, ArrayList<Uri> source, final OnKeyValueResultCallbackListener call) {
            Luban.with(context).load(source).ignoreBy(100).setRenameListener(new OnRenameListener() {
                @Override
                public String rename(String filePath) {
                    int indexOf = filePath.lastIndexOf(".");
                    String postfix = indexOf != -1 ? filePath.substring(indexOf) : ".jpg";
                    return DateUtils.getCreateFileName("CMP_") + postfix;
                }
            }).setCompressListener(new OnNewCompressListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onSuccess(String source, File compressFile) {
                    if (call != null) {
                        call.onCallback(source, compressFile.getAbsolutePath());
                    }
                }

                @Override
                public void onError(String source, Throwable e) {
                    if (call != null) {
                        call.onCallback(source, null);
                    }
                }
            }).launch();
        }
    }

    /**
     * 自定义沙盒文件处理
     */
    private static class MeSandboxFileEngine implements UriToFileTransformEngine {

        @Override
        public void onUriToFileAsyncTransform(Context context, String srcPath, String mineType, OnKeyValueResultCallbackListener call) {
            if (call != null) {
                call.onCallback(srcPath, SandboxTransformUtils.copyPathToSandbox(context, srcPath, mineType));
            }
        }
    }

    /**
     * 拦截自定义提示
     */
    private static class MeOnSelectLimitTipsListener implements OnSelectLimitTipsListener {

        @Override
        public boolean onSelectLimitTips(Context context, PictureSelectionConfig config, int limitType) {
            if (limitType == SelectLimitType.SELECT_NOT_SUPPORT_SELECT_LIMIT) {
                ToastUtils.show(context, "暂不支持的选择类型");
                return true;
            }
            return false;
        }
    }
}
