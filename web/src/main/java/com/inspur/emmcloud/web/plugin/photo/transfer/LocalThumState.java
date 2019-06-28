package com.inspur.emmcloud.web.plugin.photo.transfer;

import android.graphics.drawable.Drawable;

import com.inspur.emmcloud.web.plugin.photo.loader.ImageLoaderCommon;
import com.inspur.emmcloud.web.plugin.photo.view.TransferImage;

/**
 * 高清图图片已经加载过了，使用高清图作为缩略图。
 * 同时使用 {@link TransferImage#CATE_ANIMA_TOGETHER} 动画类型展示图片
 * <p>
 * Created by hitomi on 2017/5/4.
 * <p>
 * email: 196425254@qq.com
 */
class LocalThumState extends TransferState {

    LocalThumState(TransferLayout transfer) {
        super(transfer);
    }

    @Override
    public void prepareTransfer(final TransferImage transImage, final int position) {
        final TransferConfig config = transfer.getTransConfig();
        ImageLoaderCommon imageLoader = config.getImageLoader();
        String imgUrl = config.getSourceImageList().get(position);
        imageLoader.showImage(imgUrl, transImage, config.getMissDrawable(context), null);
    }

    @Override
    public TransferImage createTransferIn(final int position) {
        TransferConfig config = transfer.getTransConfig();

        TransferImage transImage = createTransferImage();
        transformThumbnail(config.getSourceImageList().get(position), transImage, true);
        transfer.addView(transImage, 1);

        return transImage;
    }

    @Override
    public void transferLoad(final int position) {
        final TransferConfig config = transfer.getTransConfig();
        final String imgUrl = config.getSourceImageList().get(position);
        final TransferImage targetImage = transfer.getTransAdapter().getImageItem(position);

        if (config.isJustLoadHitImage()) {
            // 如果用户设置了 JustLoadHitImage 属性，说明在 prepareTransfer 中已经
            // 对 TransferImage 裁剪且设置了占位图， 所以这里直接加载原图即可
            loadSourceImage(imgUrl, targetImage, targetImage.getDrawable(), position);
        } else {
            config.getImageLoader().loadImageAsync(imgUrl, new ImageLoaderCommon.ThumbnailCallback() {
                @Override
                public void onFinish(Drawable drawable) {
                    if (drawable == null)
                        drawable = config.getMissDrawable(context);

                    loadSourceImage(imgUrl, targetImage, drawable, position);
                }
            });
        }
    }

    private void loadSourceImage(String imgUrl, final TransferImage targetImage, Drawable drawable, final int position) {
        final TransferConfig config = transfer.getTransConfig();

        config.getImageLoader().showImage(imgUrl, targetImage, drawable, new ImageLoaderCommon.SourceCallback() {

            @Override
            public void onStart() {
            }

            @Override
            public void onProgress(int progress) {
            }

            @Override
            public void onFinish() {
            }

            @Override
            public void onDelivered(int status) {
                switch (status) {
                    case ImageLoaderCommon.STATUS_DISPLAY_SUCCESS:
                        if (TransferImage.STATE_TRANS_CLIP == targetImage.getState())
                            targetImage.transformIn(TransferImage.STAGE_SCALE);
                        // 启用 TransferImage 的手势缩放功能
                        targetImage.enable();
                        // 绑定点击关闭 Transferee
                        transfer.bindOnOperationListener(targetImage, position);
                        break;
                    case ImageLoaderCommon.STATUS_DISPLAY_FAILED:  // 加载失败，显示加载错误的占位图
                        targetImage.setImageDrawable(config.getErrorDrawable(context));
                        break;
                }
            }
        });
    }

}
