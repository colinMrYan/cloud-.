package com.inspur.emmcloud.web.plugin.photo.transfer;

import android.graphics.drawable.Drawable;

import com.inspur.emmcloud.web.plugin.photo.loader.ImageLoaderCommon;
import com.inspur.emmcloud.web.plugin.photo.style.IProgressIndicator;
import com.inspur.emmcloud.web.plugin.photo.view.TransferImage;

/**
 * 用户指定了缩略图路径，使用该路径加载缩略图，
 * 并使用 {@link TransferImage#CATE_ANIMA_TOGETHER} 动画类型展示图片
 * <p>
 * Created by hitomi on 2017/5/4.
 * <p>
 * email: 196425254@qq.com
 */
class RemoteThumState extends TransferState {

    RemoteThumState(TransferLayout transfer) {
        super(transfer);
    }

    @Override
    public void prepareTransfer(final TransferImage transImage, int position) {
        final TransferConfig config = transfer.getTransConfig();

        ImageLoaderCommon imageLoader = config.getImageLoader();
        String imgUrl = config.getThumbnailImageList().get(position);

        if (imageLoader.isLoaded(imgUrl)) {
            imageLoader.showImage(imgUrl, transImage, config.getMissDrawable(context), null);
        } else {
            transImage.setImageDrawable(config.getMissDrawable(context));
        }
    }

    @Override
    public TransferImage createTransferIn(final int position) {
        TransferConfig config = transfer.getTransConfig();

        TransferImage transImage = createTransferImage(
                0, 0, 0, 0);
        transformThumbnail(config.getThumbnailImageList().get(position), transImage, true);
        transfer.addView(transImage, 1);

        return transImage;
    }

    @Override
    public void transferLoad(final int position) {
        TransferAdapter adapter = transfer.getTransAdapter();
        final TransferConfig config = transfer.getTransConfig();
        final TransferImage targetImage = transfer.getTransAdapter().getImageItem(position);
        final ImageLoaderCommon imageLoader = config.getImageLoader();

        final IProgressIndicator progressIndicator = config.getProgressIndicator();
        progressIndicator.attach(position, adapter.getParentItem(position));

        if (config.isJustLoadHitImage()) {
            // 如果用户设置了 JustLoadHitImage 属性，说明在 prepareTransfer 中已经
            // 对 TransferImage 裁剪且设置了占位图， 所以这里直接加载原图即可
            loadSourceImage(targetImage.getDrawable(), position, targetImage, progressIndicator);
        } else {
            String thumUrl = config.getThumbnailImageList().get(position);
            String sourceUrl = config.getSourceImageList().get(position);

            if (imageLoader.isLoaded(sourceUrl)) {
                loadSourceImage(config.getMissDrawable(context), position, targetImage, progressIndicator);
            } else {
                imageLoader.loadImageAsync(thumUrl, new ImageLoaderCommon.ThumbnailCallback() {

                    @Override
                    public void onFinish(Drawable drawable) {
                        if (drawable == null)
                            drawable = config.getMissDrawable(context);

                        loadSourceImage(drawable, position, targetImage, progressIndicator);
                    }
                });
            }
        }
    }

    private void loadSourceImage(Drawable drawable, final int position, final TransferImage targetImage, final IProgressIndicator progressIndicator) {
        final TransferConfig config = transfer.getTransConfig();
        config.getImageLoader().showImage(config.getSourceImageList().get(position),
                targetImage, drawable, new ImageLoaderCommon.SourceCallback() {

                    @Override
                    public void onStart() {
                        progressIndicator.onStart(position);
                    }

                    @Override
                    public void onProgress(int progress) {
                        progressIndicator.onProgress(position, progress);
                    }

                    @Override
                    public void onFinish() {
                    }

                    @Override
                    public void onDelivered(int status) {
                        switch (status) {
                            case ImageLoaderCommon.STATUS_DISPLAY_SUCCESS:
                                progressIndicator.onFinish(position); // onFinish 只是说明下载完毕，并没更新图像
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
