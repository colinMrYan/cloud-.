package com.inspur.emmcloud.basemodule.media.selector.demo;

import android.os.Bundle;

import com.inspur.emmcloud.basemodule.media.selector.PictureSelectorPreviewFragment;
import com.inspur.emmcloud.basemodule.media.selector.adapter.PicturePreviewAdapter;
import com.inspur.emmcloud.basemodule.media.selector.magical.BuildRecycleItemViewParams;
import com.inspur.emmcloud.basemodule.media.selector.magical.MagicalView;
import com.inspur.emmcloud.basemodule.media.selector.magical.OnMagicalViewCallback;
import com.inspur.emmcloud.basemodule.media.selector.magical.ViewParams;
import com.inspur.emmcloud.basemodule.media.selector.widget.TitleBar;

/**
 * @author：luck
 * @date：2022/2/21 4:15 下午
 * @describe：CustomPreviewFragment
 */
public class CustomPreviewFragment extends PictureSelectorPreviewFragment {

    public static CustomPreviewFragment newInstance() {
        CustomPreviewFragment fragment = new CustomPreviewFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }

    @Override
    public String getFragmentTag() {
        return CustomPreviewFragment.class.getSimpleName();
    }

    @Override
    protected PicturePreviewAdapter createAdapter() {
        return new CustomPreviewAdapter();
    }

    @Override
    protected void setMagicalViewAction() {
        // 如果开启了isPreviewZoomEffect效果，需要重载此方法
        magicalView.setOnMojitoViewCallback(new OnMagicalViewCallback() {
            @Override
            public void onBeginBackMinAnim() {
//                BasePreviewHolder currentHolder = viewPageAdapter.getCurrentHolder(viewPager.getCurrentItem());
//                if (currentHolder == null) {
//                    return;
//                }
//                if (currentHolder.coverImageView.getVisibility() == View.GONE) {
//                    currentHolder.coverImageView.setVisibility(View.VISIBLE);
//                }
//                if (currentHolder instanceof PreviewVideoHolder) {
//                    PreviewVideoHolder videoHolder = (PreviewVideoHolder) currentHolder;
//                    if (videoHolder.ivPlayButton.getVisibility() == View.VISIBLE) {
//                        videoHolder.ivPlayButton.setVisibility(View.GONE);
//                    }
//                }
            }

            @Override
            public void onBeginBackMinMagicalFinish(boolean isResetSize) {
                ViewParams itemViewParams = BuildRecycleItemViewParams.getItemViewParams(isShowCamera ? curPosition + 1 : curPosition);
                if (itemViewParams == null) {
                    return;
                }
//                BasePreviewHolder currentHolder = viewPageAdapter.getCurrentHolder(viewPager.getCurrentItem());
//                if (currentHolder == null) {
//                    return;
//                }
//                currentHolder.coverImageView.getLayoutParams().width = itemViewParams.width;
//                currentHolder.coverImageView.getLayoutParams().height = itemViewParams.height;
//                currentHolder.coverImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }

            @Override
            public void onBeginMagicalAnimComplete(MagicalView mojitoView, boolean showImmediately) {
//                BasePreviewHolder currentHolder = viewPageAdapter.getCurrentHolder(viewPager.getCurrentItem());
//                if (currentHolder == null) {
//                    return;
//                }
//                LocalMedia media = mData.get(viewPager.getCurrentItem());
//                int realWidth, realHeight;
//                if (media.isCut() && media.getCropImageWidth() > 0 && media.getCropImageHeight() > 0) {
//                    realWidth = media.getCropImageWidth();
//                    realHeight = media.getCropImageHeight();
//                } else {
//                    realWidth = media.getWidth();
//                    realHeight = media.getHeight();
//                }
//                if (MediaUtils.isLongImage(realWidth, realHeight)) {
//                    currentHolder.coverImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
//                } else {
//                    currentHolder.coverImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                }
//                if (currentHolder instanceof PreviewVideoHolder) {
//                    PreviewVideoHolder videoHolder = (PreviewVideoHolder) currentHolder;
//                    if (videoHolder.ivPlayButton.getVisibility() == View.GONE) {
//                        videoHolder.ivPlayButton.setVisibility(View.VISIBLE);
//                    }
//                }
            }

            @Override
            public void onBackgroundAlpha(float alpha) {
                for (int i = 0; i < mAnimViews.size(); i++) {
                    if (mAnimViews.get(i) instanceof TitleBar) {
                        continue;
                    }
                    mAnimViews.get(i).setAlpha(alpha);
                }
            }

            @Override
            public void onMagicalViewFinish() {
                onBackCurrentFragment();
            }
        });
    }
}
