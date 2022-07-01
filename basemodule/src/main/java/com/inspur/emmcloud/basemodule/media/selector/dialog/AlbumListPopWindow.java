package com.inspur.emmcloud.basemodule.media.selector.dialog;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.basemodule.R;
import com.inspur.emmcloud.basemodule.media.selector.adapter.PictureAlbumAdapter;
import com.inspur.emmcloud.basemodule.media.selector.config.PictureConfig;
import com.inspur.emmcloud.basemodule.media.selector.decoration.WrapContentLinearLayoutManager;
import com.inspur.emmcloud.basemodule.media.selector.entity.LocalMedia;
import com.inspur.emmcloud.basemodule.media.selector.entity.LocalMediaFolder;
import com.inspur.emmcloud.basemodule.media.selector.interfaces.OnAlbumItemClickListener;
import com.inspur.emmcloud.basemodule.media.selector.manager.SelectedManager;
import com.inspur.emmcloud.basemodule.media.selector.utils.DensityUtil;
import com.inspur.emmcloud.basemodule.media.selector.utils.SdkVersionUtils;

import java.util.List;

/**
 * @author：luck
 * @date：2021/11/17 2:33 下午
 * @describe：AlbumListPopWindow
 */
public class AlbumListPopWindow extends PopupWindow {
    private static final int ALBUM_MAX_COUNT = 8;
    private final Context mContext;
    private View windMask, marginX;
    private RecyclerView mRecyclerView;
    private boolean isDismiss = false;
    private int windowMaxHeight;
    private PictureAlbumAdapter mAdapter;
    private int marginPx;
    private boolean firstTimeLoad = true;
    private boolean onGoingDismiss = false;

    public AlbumListPopWindow(Context context) {
        this.mContext = context;
        setContentView(LayoutInflater.from(context).inflate(R.layout.ps_window_folder, null));
        setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);
        setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
//        setAnimationStyle(R.style.PictureThemePickerDialogWindowStyle);
        setAnimationStyle(0);
        setBackgroundDrawable(new ColorDrawable(0));
        setFocusable(true);
        setOutsideTouchable(true);
        update();
        initViews();
    }

    private void initViews() {
        windowMaxHeight = (int) (DensityUtil.getScreenHeight(mContext) * 0.8);
        mRecyclerView = getContentView().findViewById(R.id.folder_list);
        windMask = getContentView().findViewById(R.id.rootViewBg);
        marginX = getContentView().findViewById(R.id.margin);
        mRecyclerView.setLayoutManager(new WrapContentLinearLayoutManager(mContext));
        mAdapter = new PictureAlbumAdapter();
        mRecyclerView.setAdapter(mAdapter);
        marginX.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        windMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        getContentView().findViewById(R.id.rootView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SdkVersionUtils.isMinM()) {
                    dismiss();
                }
            }
        });
    }

    private void enterAnimator() {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(windMask, "alpha", 0, 1);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(mRecyclerView, "translationY",  -mRecyclerView.getHeight(), 0);
        AnimatorSet set = new AnimatorSet();
        set.setDuration(300);
        set.playTogether(alpha, translationY);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.start();
    }

    private void exitAnimator(final LocalMediaFolder curFolder) {
        ObjectAnimator alpha = ObjectAnimator.ofFloat(windMask, "alpha", 1, 0);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(mRecyclerView, "translationY",  0, -mRecyclerView.getHeight());
        AnimatorSet set = new AnimatorSet();
        set.setDuration(300);
        set.playTogether(alpha, translationY);
        set.setInterpolator(new AccelerateDecelerateInterpolator());
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                mRecyclerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                AlbumListPopWindow.super.dismiss();
//                isDismiss = false;
                if (curFolder != null && windowStatusListener != null) {
                    windowStatusListener.onDismissPopupWindow(curFolder);
                }
                onGoingDismiss = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onGoingDismiss = false;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        set.start();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void bindAlbumData(List<LocalMediaFolder> list) {
        mAdapter.bindAlbumData(list);
        mAdapter.notifyDataSetChanged();
        ViewGroup.LayoutParams lp = mRecyclerView.getLayoutParams();
        lp.height = list.size() > ALBUM_MAX_COUNT ? windowMaxHeight : ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public List<LocalMediaFolder> getAlbumList() {
        return mAdapter.getAlbumList();
    }

    public LocalMediaFolder getFolder(int position) {
        return mAdapter.getAlbumList().size() > 0
                && position < mAdapter.getAlbumList().size() ? mAdapter.getAlbumList().get(position) : null;
    }

    public int getFirstAlbumImageCount() {
        return getFolderCount() > 0 ? getFolder(0).getFolderTotalNum() : 0;
    }

    public int getFolderCount() {
        return mAdapter.getAlbumList().size();
    }

    /**
     * 专辑列表桥接类
     *
     * @param listener
     */
    public void setOnIBridgeAlbumWidget(OnAlbumItemClickListener listener) {
        mAdapter.setOnIBridgeAlbumWidget(listener);
    }

    public static AlbumListPopWindow buildPopWindow(Context context) {
        return new AlbumListPopWindow(context);
    }

    @Override
    public void showAsDropDown(View anchor) {
        if (getAlbumList() == null || getAlbumList().size() == 0) {
            return;
        }
        if (SdkVersionUtils.isN()) {
            int[] location = new int[2];
            anchor.getLocationInWindow(location);
            showAtLocation(anchor, Gravity.NO_GRAVITY, 0, location[1] + anchor.getHeight());
        } else {
            super.showAsDropDown(anchor);
        }
//        isDismiss = false;
        if (windowStatusListener != null) {
            windowStatusListener.onShowPopupWindow();
        }
        final View rootView = getContentView();
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                rootView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (firstTimeLoad) {
                    int maxHeight = rootView.getHeight() * 4 / 5;
                    int realHeight = mRecyclerView.getHeight();
                    ViewGroup.LayoutParams listParams = mRecyclerView.getLayoutParams();
                    listParams.height = realHeight > maxHeight ? maxHeight : realHeight;
                    mRecyclerView.setLayoutParams(listParams);
                    LinearLayout.LayoutParams marginParams = (LinearLayout.LayoutParams) marginX.getLayoutParams();
                    marginParams.height = marginPx;
                    marginX.setLayoutParams(marginParams);
                    firstTimeLoad = false;
                }
                enterAnimator();
            }
        });
        windMask.animate().alpha(1).setDuration(200).setStartDelay(200).start();
        changeSelectedAlbumStyle();
    }

    /**
     * 设置选中状态
     */
    public void changeSelectedAlbumStyle() {
        List<LocalMediaFolder> folders = mAdapter.getAlbumList();
        for (int i = 0; i < folders.size(); i++) {
            LocalMediaFolder folder = folders.get(i);
            folder.setSelectTag(false);
            mAdapter.notifyItemChanged(i);
            for (int j = 0; j < SelectedManager.getSelectCount(); j++) {
                LocalMedia media = SelectedManager.getSelectedResult().get(j);
                if (TextUtils.equals(folder.getFolderName(), media.getParentFolderName())
                        || folder.getBucketId() == PictureConfig.ALL) {
                    folder.setSelectTag(true);
                    mAdapter.notifyItemChanged(i);
                    break;
                }
            }
        }
    }


    public void setMarginPx(int marginPx) {
        this.marginPx = marginPx;
    }

    @Override
    public void dismiss() {
//        if (isDismiss) {
//            return;
//        }
//        isDismiss = true;
//        windMask.post(new Runnable() {
//            @Override
//            public void run() {
//                AlbumListPopWindow.super.dismiss();
//            }
//        });
        windMask.setAlpha(0F);
        windowStatusListener.onDismissPopupWindow(null);
        exitAnimator(null);
    }

    public void dismissWithCallback(LocalMediaFolder curFolder){
        if (onGoingDismiss) return;
        onGoingDismiss = true;
        windMask.setAlpha(0F);
        exitAnimator(curFolder);
    }


    /**
     * AlbumListPopWindow 弹出与消失状态监听
     *
     * @param listener
     */
    public void setOnPopupWindowStatusListener(OnPopupWindowStatusListener listener) {
        this.windowStatusListener = listener;
    }

    private OnPopupWindowStatusListener windowStatusListener;

    public interface OnPopupWindowStatusListener {

        void onShowPopupWindow();

        void onDismissPopupWindow(LocalMediaFolder curFolder);
    }
}
