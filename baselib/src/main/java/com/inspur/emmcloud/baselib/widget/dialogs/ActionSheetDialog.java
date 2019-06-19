package com.inspur.emmcloud.baselib.widget.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.baselib.R;

import java.util.ArrayList;
import java.util.List;


public class ActionSheetDialog extends Dialog {
    private static final String TAG = "ActionSheetDialog";

    // 动画时长
    private final static int mAnimationDuration = 200;
    // 持有 ContentView，为了做动画
    private View mContentView;
    private boolean mIsAnimating = false;

    private OnActionSheetShowListener mOnActionSheetShowListener;

    public ActionSheetDialog(Context context) {
        super(context, R.style.transparentFrameWindowStyle);
    }

    public void setOnActionSheetShowListener(OnActionSheetShowListener mOnActionSheetShowListener) {
        this.mOnActionSheetShowListener = mOnActionSheetShowListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setPadding(0, 0, 0, 0);

        // 在底部，宽度撑满
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = Gravity.BOTTOM | Gravity.CENTER;
        getWindow().setWindowAnimations(R.style.main_menu_animstyle);
        Resources resources = getContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        params.width = screenWidth < screenHeight ? screenWidth : screenHeight;
        params.width = (int) (params.width * 0.9);
        params.dimAmount = 0.31f;
        getWindow().setAttributes(params);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        setCanceledOnTouchOutside(true);
    }

    @Override
    public void setContentView(int layoutResID) {
        mContentView = LayoutInflater.from(getContext()).inflate(layoutResID, null);
        super.setContentView(mContentView);
    }

    @Override
    public void setContentView(@NonNull View view, ViewGroup.LayoutParams params) {
        mContentView = view;
        super.setContentView(view, params);
    }


    @Override
    public void setContentView(@NonNull View view) {
        mContentView = view;
        super.setContentView(view);
    }


    @Override
    public void show() {
        super.show();
        if (mOnActionSheetShowListener != null) {
            mOnActionSheetShowListener.onShow();
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public interface OnActionSheetShowListener {
        void onShow();
    }

    /**
     * 生成列表类型的 {@link ActionSheetDialog} 对话框。
     */
    public static class ActionListSheetBuilder {

        private Context mContext;

        private ActionSheetDialog mDialog;
        private List<ActionSheetListItemData> mItems;
        private BaseAdapter mAdapter;
        private List<View> mHeaderViews;
        private ListView mContainerView;
        private int mCheckedIndex;
        private String mTitle;
        private TextView mTitleTv;
        private OnSheetItemClickListener mOnSheetItemClickListener;
        private DialogInterface.OnDismissListener mOnActionSheetDlgDismissListener;
        private int titleColor = Color.parseColor("#333333");
        private int itemColor = Color.parseColor("#333333");
        private int cancelColor = Color.parseColor("#36A5F6");

        public ActionListSheetBuilder(Context context) {
            mContext = context;
            mItems = new ArrayList<>();
            mHeaderViews = new ArrayList<>();
        }


        /**
         * 设置要被选中的 Item 的下标。
         * <p>
         */
        public ActionListSheetBuilder setCheckedIndex(int checkedIndex) {
            mCheckedIndex = checkedIndex;
            return this;
        }

        /**
         * @param textAndTag Item 的文字内容，同时会把内容设置为 tag。
         */
        public ActionListSheetBuilder addItem(String textAndTag) {
            mItems.add(new ActionSheetListItemData(textAndTag, textAndTag));
            return this;
        }

        public ActionListSheetBuilder addItem(String textAndTag, boolean isShow) {
            if (isShow) {
                mItems.add(new ActionSheetListItemData(textAndTag, textAndTag, isShow));
            }
            return this;
        }

        public ActionListSheetBuilder setOnSheetItemClickListener(OnSheetItemClickListener onSheetItemClickListener) {
            mOnSheetItemClickListener = onSheetItemClickListener;
            return this;
        }

        public ActionListSheetBuilder setOnActionSheetDlgDismissListener(DialogInterface.OnDismissListener listener) {
            mOnActionSheetDlgDismissListener = listener;
            return this;
        }

        public ActionListSheetBuilder addHeaderView(View view) {
            if (view != null) {
                mHeaderViews.add(view);
            }
            return this;
        }

        public ActionListSheetBuilder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public ActionListSheetBuilder setTitle(int resId) {
            mTitle = mContext.getResources().getString(resId);
            return this;
        }

        /**
         * 设置标题颜色
         *
         * @param titleColor
         */
        public ActionListSheetBuilder setTitleColor(int titleColor) {
            this.titleColor = titleColor;
            return this;
        }

        /**
         * 设置条目颜色
         *
         * @param itemColor
         */
        public ActionListSheetBuilder setItemColor(int itemColor) {
            this.itemColor = itemColor;
            return this;
        }

        /**
         * 设置取消键颜色
         *
         * @param cancelColor
         */
        public ActionListSheetBuilder setCancelColor(int cancelColor) {
            this.cancelColor = cancelColor;
            return this;
        }

        public ActionSheetDialog build() {
            mDialog = new ActionSheetDialog(mContext);
            View contentView = buildViews();
            mDialog.setContentView(contentView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            if (mOnActionSheetDlgDismissListener != null) {
                mDialog.setOnDismissListener(mOnActionSheetDlgDismissListener);
            }
            return mDialog;
        }

        @SuppressLint("ResourceAsColor")
        private View buildViews() {
            View wrapperView = View.inflate(mContext, R.layout.basewidget_actionsheet, null);
            mTitleTv = (TextView) wrapperView.findViewById(R.id.title);
            mTitleTv.setTextColor(titleColor);
            mContainerView = (ListView) wrapperView.findViewById(R.id.sheetList);
            if (mTitle != null && mTitle.length() != 0) {
                (wrapperView.findViewById(R.id.title_layout)).setVisibility(View.VISIBLE);
                mTitleTv.setText(mTitle);
            } else {
                (wrapperView.findViewById(R.id.title_layout)).setVisibility(View.GONE);
            }
            if (mHeaderViews.size() > 0) {
                for (View headerView : mHeaderViews) {
                    mContainerView.addHeaderView(headerView);
                }
            }

            mAdapter = new ListAdapter();
            mContainerView.setAdapter(mAdapter);
            (wrapperView.findViewById(R.id.cancel)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
            ((TextView) wrapperView.findViewById(R.id.cancel)).setTextColor(cancelColor);
            return wrapperView;
        }


        public void notifyDataSetChanged() {
            if (mAdapter != null) {
                mAdapter.notifyDataSetChanged();
            }
        }


        public interface OnSheetItemClickListener {
            void onClick(ActionSheetDialog dialog, View itemView, int position);
        }

        private static class ActionSheetListItemData {

            String text;
            String tag = "";
            boolean isShow = true;

            public ActionSheetListItemData(String text, String tag) {
                this.text = text;
                this.tag = tag;
            }

            public ActionSheetListItemData(String text, String tag, boolean isShow) {
                this.text = text;
                this.tag = tag;
                this.isShow = isShow;
            }
        }


        private class ListAdapter extends BaseAdapter {

            @Override
            public int getCount() {
                return mItems.size();
            }

            @Override
            public ActionSheetListItemData getItem(int position) {
                return mItems.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                final ActionSheetListItemData data = getItem(position);
                convertView = LayoutInflater.from(mContext).inflate(R.layout.basewidget_actionsheet_item_view, parent, false);
                TextView textView = (TextView) convertView.findViewById(R.id.content);
                textView.setTextColor(itemColor);
                textView.setText(data.text);
                convertView.findViewById(R.id.layout).setVisibility(data.isShow ? View.VISIBLE : View.GONE);
                convertView.setTag(data.text);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnSheetItemClickListener != null) {
                            mOnSheetItemClickListener.onClick(mDialog, v, position);
                        }
                    }
                });
                return convertView;
            }
        }

    }

}
