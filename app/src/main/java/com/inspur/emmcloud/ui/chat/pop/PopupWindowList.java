package com.inspur.emmcloud.ui.chat.pop;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.inspur.emmcloud.basemodule.util.LanguageManager;

import java.util.List;

/**
 * 仿微信长按消息  弹框
 */
public class PopupWindowList {
    private Context mContext;
    private PopupWindow mPopupWindow;
    //the view where PopupWindow lie in
    private View mAnchorView;
    //ListView item data
    private List<String> mItemData;
    //the animation for PopupWindow
    private int mPopAnimStyle;
    //the PopupWindow width
    private int mPopupWindowWidth;
    //the PopupWindow height
    private int mPopupWindowHeight;
    private AdapterView.OnItemClickListener mItemClickListener;
    private boolean mModal;
    private ListView mPopView;
    private int mDeviceWidth, mDeviceHeight;

    public PopupWindowList(Context mContext) {
        if (mContext == null) {
            throw new IllegalArgumentException("context can not be null");
        }
        this.mContext = mContext;
        setHeightWidth();
    }

    public static void setBackgroundAlpha(Activity activity, float bgAlpha) {
        WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
        lp.alpha = bgAlpha;
        if (bgAlpha == 1) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
        } else {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);//此行代码主要是解决在华为手机上半透明效果无效的bug
        }
        activity.getWindow().setAttributes(lp);
    }

    public void setAnchorView(@Nullable View anchor) {
        mAnchorView = anchor;
    }

    public void setItemData(List<String> mItemData) {
        this.mItemData = mItemData;
    }

    public void setPopAnimStyle(int mPopAnimStyle) {
        this.mPopAnimStyle = mPopAnimStyle;
    }

    public void setPopupWindowWidth(int mPopupWindowWidth) {
        this.mPopupWindowWidth = mPopupWindowWidth;
    }

    public void setPopupWindowHeight(int mPopupWindowHeight) {
        this.mPopupWindowHeight = mPopupWindowHeight;
    }

    /**
     * Set whether this window should be modal when shown.
     *
     * <p>If a popup window is modal, it will receive all touch and key input.
     * If the user touches outside the popup window's content area the popup window
     * will be dismissed.
     *
     * @param modal {@code true} if the popup window should be modal, {@code false} otherwise.
     */
    public void setModal(boolean modal) {
        mModal = modal;
    }

    public boolean isShowing() {
        return mPopupWindow != null && mPopupWindow.isShowing();
    }

    public void hide() {
        if (isShowing()) {
            mPopupWindow.dismiss();
            if (mContext != null)
                setBackgroundAlpha((Activity) mContext, 1f);
        }
    }

    /**
     * Sets a listener to receive events when a list item is clicked.
     *
     * @param clickListener Listener to register
     * @see ListView#setOnItemClickListener(AdapterView.OnItemClickListener)
     */
    public void setOnItemClickListener(@Nullable AdapterView.OnItemClickListener clickListener) {
        mItemClickListener = clickListener;
        if (mPopView != null) {
            mPopView.setOnItemClickListener(mItemClickListener);
        }
    }

    public void show() {
        if (mAnchorView == null) {
            throw new IllegalArgumentException("PopupWindow show location view can  not be null");
        }
        if (mItemData == null) {
            throw new IllegalArgumentException("please fill ListView Data");
        }
        mPopView = new ListView(mContext);
        mPopView.setBackgroundColor(ContextCompat.getColor(mContext, android.R.color.white));
        mPopView.setVerticalScrollBarEnabled(false);
        mPopView.setDivider(null);
        mPopView.setAdapter(new ArrayAdapter<>(mContext,
                android.R.layout.simple_list_item_1, mItemData));
        if (mItemClickListener != null) {
            mPopView.setOnItemClickListener(mItemClickListener);
        }
        mPopView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        String languageName = LanguageManager.getInstance().getCurrentLanguageName();
        if (languageName.equals("en-US")) {
            mPopupWindowWidth = (int) (mDeviceWidth * 0.45);
        } else {
            mPopupWindowWidth = mDeviceWidth / 3;
        }

        mPopupWindowHeight = mItemData.size() * mPopView.getMeasuredHeight();
        if (mPopupWindowHeight > mDeviceHeight / 2) {
            mPopupWindowHeight = mDeviceHeight / 2;
        }
        mPopupWindow = new PopupWindow(mPopView, mPopupWindowWidth, mPopupWindowHeight);
        if (mPopAnimStyle != 0) {
            mPopupWindow.setAnimationStyle(mPopAnimStyle);
        }
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(mModal);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), (Bitmap) null));
        if (mContext != null)
            setBackgroundAlpha((Activity) mContext, 0.8f);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mContext != null) {
                    setBackgroundAlpha((Activity) mContext, 1f);
                }
            }
        });


        Rect location = locateView(mAnchorView);
        if (location != null) {
            int x;
            //view中心点X坐标
            int xMiddle = location.left + mAnchorView.getWidth() / 2;
            if (xMiddle > mDeviceWidth / 2) {
                //在右边
                x = xMiddle - mPopupWindowWidth;
            } else {
                x = xMiddle;
            }
            int y;
            //view中心点Y坐标
            int yMiddle = location.top + mAnchorView.getHeight() / 2;
            if (yMiddle > mDeviceHeight / 2) {
                //在下方
                y = yMiddle - mPopupWindowHeight;
            } else {
                //在上方
                y = yMiddle;
            }
            mPopupWindow.showAtLocation(mAnchorView, Gravity.NO_GRAVITY, x, y);
        }
    }

    public Rect locateView(View v) {
        if (v == null) return null;
        int[] loc_int = new int[2];
        try {
            v.getLocationOnScreen(loc_int);
        } catch (NullPointerException npe) {
            //Happens when the view doesn't exist on screen anymore.
            return null;
        }
        Rect location = new Rect();
        location.left = loc_int[0];
        location.top = loc_int[1];
        location.right = location.left + v.getWidth();
        location.bottom = location.top + v.getHeight();
        return location;
    }

    public void setHeightWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Point outSize = new Point();
        wm.getDefaultDisplay().getSize(outSize);
        if (outSize.x != 0) {
            mDeviceWidth = outSize.x;
        }
        if (outSize.y != 0) {
            mDeviceHeight = outSize.y;
        }
    }
}
