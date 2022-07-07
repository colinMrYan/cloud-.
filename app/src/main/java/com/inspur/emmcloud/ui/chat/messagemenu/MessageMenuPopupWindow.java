package com.inspur.emmcloud.ui.chat.messagemenu;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.baselib.util.DensityUtil;
import com.inspur.emmcloud.basemodule.widget.bubble.ArrowDirection;
import com.inspur.emmcloud.basemodule.widget.bubble.BubbleLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * 仿微信长按消息  弹框
 */
public class MessageMenuPopupWindow {
    private Context mContext;
    private PopupWindow mPopupWindow;
    //the view where PopupWindow lie in
    private View mAnchorView;
    //ListView item data
    private List<MessageMenuItem> mItemData;
    //the animation for PopupWindow
    private int mPopAnimStyle;
    //the PopupWindow width
    private int mPopupWindowWidth;
    //the PopupWindow height
    private int mPopupWindowHeight;
    private PopItemClickListener mItemClickListener;
    private boolean mModal;
    private View mPopMenu;
    private RecyclerView mRecyclerView;
    private MessageMenuAdapter mPopViewAdapter;
    private BubbleLayout mBubbleLayout;
    private int mDeviceWidth, mDeviceHeight;

    public MessageMenuPopupWindow(Context mContext) {
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

    public void setItemData(List<String> stringListData) {
        mItemData = new ArrayList<>();
        for (String text : stringListData) {
            if (text.equals(mContext.getString(R.string.chat_resend_message))) {
                mItemData.add(new MessageMenuItem(R.drawable.message_menu_resend, text));
            } else if (text.equals(mContext.getString(R.string.delete))) {
                mItemData.add(new MessageMenuItem(R.drawable.message_menu_delete, text));
            } else if (text.equals(mContext.getString(R.string.chat_long_click_copy))) {
                mItemData.add(new MessageMenuItem(R.drawable.message_menu_copy, text));
            } else if (text.equals(mContext.getString(R.string.chat_long_click_transmit))) {
                mItemData.add(new MessageMenuItem(R.drawable.message_menu_transfer, text));
            } else if (text.equals(mContext.getString(R.string.chat_long_click_reply))) {
                mItemData.add(new MessageMenuItem(R.drawable.message_menu_reply, text));
            } else if (text.equals(mContext.getString(R.string.chat_long_click_schedule))) {
                mItemData.add(new MessageMenuItem(R.drawable.message_menu_schedule, text));
            } else if (text.equals(mContext.getString(R.string.chat_long_click_multiple))) {
                mItemData.add(new MessageMenuItem(R.drawable.message_menu_multi_select, text));
            } else if (text.equals(mContext.getString(R.string.voice_to_word))) {
                mItemData.add(new MessageMenuItem(R.drawable.message_menu_translate, text));
            } else if (text.equals(mContext.getString(R.string.chat_long_click_recall))) {
                mItemData.add(new MessageMenuItem(R.drawable.message_menu_revoke, text));
            }
        }
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

    public interface PopItemClickListener {
        void onPopItemClick(int position);
    }

    /**
     * Sets a listener to receive events when a list item is clicked.
     *
     * @param clickListener Listener to register
     * @see ListView#setOnItemClickListener(AdapterView.OnItemClickListener)
     */
    public void setOnItemClickListener(@Nullable PopItemClickListener clickListener) {
        mItemClickListener = clickListener;
        if (mPopViewAdapter != null) {
            mPopViewAdapter.setPopItemClickListener(mItemClickListener);
        }
    }

    public void show() {
        if (mAnchorView == null) {
            throw new IllegalArgumentException("PopupWindow show location view can  not be null");
        }
        if (mItemData == null) {
            throw new IllegalArgumentException("please fill ListView Data");
        }
        mPopMenu = LayoutInflater.from(mContext).inflate(R.layout.pop_menu_layout, null);
        mBubbleLayout = mPopMenu.findViewById(R.id.message_menu_bubble);
        mRecyclerView = mPopMenu.findViewById(R.id.pop_menu_recycler);
        int spanCount = Math.min(mItemData.size(), 5);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(mContext, spanCount);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mPopViewAdapter = new MessageMenuAdapter(mContext, mItemData);
        mRecyclerView.setAdapter(mPopViewAdapter);
        mPopViewAdapter.notifyDataSetChanged();
        if (mItemClickListener != null) {
            mPopViewAdapter.setPopItemClickListener(mItemClickListener);
        }
        int dpPxs = DensityUtil.dip2px(1);
        mPopupWindowWidth = (Math.min(mItemData.size(), spanCount)) * 64 * dpPxs + 20 * dpPxs;
        mPopupWindowHeight = (mItemData.size() / spanCount + (mItemData.size() % spanCount > 0 ? 1 : 0)) * 64 * dpPxs;

        mPopupWindow = new PopupWindow(mPopMenu, mPopupWindowWidth, mPopupWindowHeight);
        if (mPopAnimStyle != 0) {
            mPopupWindow.setAnimationStyle(mPopAnimStyle);
        }
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(mModal);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(mContext.getResources(), (Bitmap) null));
        final Rect location = locateView(mAnchorView);
        if (location == null) {
            return;
        }
        //靠下，向上箭头
        if ((location.top + location.bottom) / 2 >= mDeviceHeight / 2) {
            mPopupWindow.showAtLocation(mAnchorView, Gravity.NO_GRAVITY,
                    (location.left + location.right) / 2 - (mPopupWindowWidth - 20 * dpPxs) / 2, location.top - mPopupWindowHeight - 2 * dpPxs);
            //太靠右
            if ((location.left + location.right) / 2 + mPopupWindowWidth / 2 > mDeviceWidth) {
                mBubbleLayout.setArrowDirection(ArrowDirection.BOTTOM);
                mBubbleLayout.setArrowPosition(mPopupWindowWidth - 72 * dpPxs - (int) (location.width() / 2));
                //太靠左
            } else if ((location.left + location.right) / 2 - mPopupWindowWidth / 2 < 0) {
                mBubbleLayout.setArrowDirection(ArrowDirection.BOTTOM);
                mBubbleLayout.setArrowPosition(40 * dpPxs + (int) (location.width() / 2));
            } else {
                mBubbleLayout.setArrowDirection(ArrowDirection.BOTTOM_CENTER);
            }
        } else {
            mPopupWindow.showAtLocation(mAnchorView, Gravity.NO_GRAVITY,
                    (location.left + location.right) / 2 - (mPopupWindowWidth - 20 * dpPxs) / 2, location.bottom + 2 * dpPxs);
            //太靠右
            if ((location.left + location.right) / 2 + mPopupWindowWidth / 2 > mDeviceWidth) {
                mBubbleLayout.setArrowDirection(ArrowDirection.TOP);
                mBubbleLayout.setArrowPosition(mPopupWindowWidth - 72 * dpPxs - (int) (location.width() / 2));
                //太靠左
            } else if ((location.left + location.right) / 2 - mPopupWindowWidth / 2 < 0) {
                mBubbleLayout.setArrowDirection(ArrowDirection.TOP);
                mBubbleLayout.setArrowPosition(40 * dpPxs + (int) (location.width() / 2));
            } else {
                mBubbleLayout.setArrowDirection(ArrowDirection.TOP_CENTER);
            }
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
