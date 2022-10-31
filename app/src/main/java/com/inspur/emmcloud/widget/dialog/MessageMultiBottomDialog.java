package com.inspur.emmcloud.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspur.baselib.R;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.widget.ImageViewRound;
import com.inspur.emmcloud.baselib.widget.NoScrollGridView;
import com.inspur.emmcloud.baselib.widget.SquareLayout;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.componentservice.communication.Conversation;
import com.inspur.emmcloud.util.privates.CommunicationUtils;

import java.util.ArrayList;

/**
 * Date：2022/9/15
 * Author：wang zhen
 * Description 消息转发弹框
 */
public class MessageMultiBottomDialog extends Dialog {
    private View mContentView;

    public MessageMultiBottomDialog(@NonNull Context context) {
        super(context, R.style.transparentFrameWindowStyle);
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
        params.width = screenWidth;
//        params.width = Math.min(screenWidth, screenHeight);
        params.dimAmount = 0.40f;
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


    public static class MessageMultiOperateBuilder {
        private Context mContext;

        private MessageMultiBottomDialog mDialog;
        private NoScrollGridView functionView;
        private NoScrollGridView transmitView;
        private ArrayList<String> functionList;
        private ArrayList<Conversation> transmitList;
        private OnItemClickListener onItemClickListener;
        private FunctionAdapter functionAdapter; // 功能adapter
        private TransmitAdapter transmitAdapter; // 最近转发adapter
        private int currentThemeNo;

        public MessageMultiOperateBuilder(Context context) {
            mContext = context;
        }

        public MessageMultiOperateBuilder setOnItemClickListener(OnItemClickListener itemClickListener) {
            onItemClickListener = itemClickListener;
            return this;
        }

        public interface OnItemClickListener {
            void onClick(MessageMultiBottomDialog dialog, View itemView, int position);
            void onTransmitClick(MessageMultiBottomDialog dialog, View itemView, int position);
        }

        public MessageMultiOperateBuilder setFunctionList(ArrayList<String> functionList) {
            this.functionList = functionList;
            return this;
        }

        public MessageMultiOperateBuilder setTransmitList(ArrayList<Conversation> transmitList) {
            this.transmitList = transmitList;
            return this;
        }

        public MessageMultiBottomDialog build() {
            mDialog = new MessageMultiBottomDialog(mContext);
            View contentView = buildViews();
            mDialog.setContentView(contentView,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            return mDialog;
        }

        private View buildViews() {
            currentThemeNo = PreferencesUtils.getInt(mContext, "app_theme_num_v1", 0);
            View dialogView = View.inflate(mContext, R.layout.dialog_message_multi_operate, null);
            functionView = (NoScrollGridView) dialogView.findViewById(R.id.gv_function);
            LinearLayout bgLl = (LinearLayout) dialogView.findViewById(R.id.ll_bg);
            View transmitLine = (View) dialogView.findViewById(R.id.line_transmit);
            LinearLayout transmitLl = (LinearLayout) dialogView.findViewById(R.id.ll_transmit);
            transmitView = (NoScrollGridView) dialogView.findViewById(R.id.gv_transmit);
            View dialogLine = (View) dialogView.findViewById(R.id.line_dialog);
            TextView transmitTv = (TextView) dialogView.findViewById(R.id.tv_transmit);
            TextView cancelTv = (TextView) dialogView.findViewById(R.id.cancel);
            if (transmitList != null && transmitList.size() > 0) {
                transmitLl.setVisibility(View.VISIBLE);
                transmitAdapter = new TransmitAdapter();
                transmitView.setAdapter(transmitAdapter);
                transmitView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        if (onItemClickListener != null) {
                            onItemClickListener.onTransmitClick(mDialog, view, position);
                        }
                    }
                });
            } else {
                transmitLl.setVisibility(View.GONE);
            }

            functionAdapter = new FunctionAdapter();
            functionView.setAdapter(functionAdapter);
            functionView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onClick(mDialog, view, position);
                    }
                }
            });
            cancelTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDialog.dismiss();
                }
            });
            transmitLine.setBackgroundColor(currentThemeNo == 3 ? mContext.getResources().getColor(R.color.text_color) :
                    mContext.getResources().getColor(R.color.color_d1));
            dialogLine.setBackgroundColor(currentThemeNo == 3 ? mContext.getResources().getColor(R.color.text_color) :
                    mContext.getResources().getColor(R.color.color_d1));
            transmitTv.setTextColor(currentThemeNo == 3 ? mContext.getResources().getColor(R.color.bg_color_layer_layer) :
                    mContext.getResources().getColor(R.color.color_1a));
            cancelTv.setTextColor(currentThemeNo == 3 ? mContext.getResources().getColor(R.color.bg_color_layer_layer) :
                    mContext.getResources().getColor(R.color.color_1a));
            bgLl.setBackground(currentThemeNo == 3 ? mContext.getResources().getDrawable(R.drawable.bg_more_function_dark) :
                    mContext.getResources().getDrawable(R.drawable.bg_more_function));
            return dialogView;
        }

        private class FunctionAdapter extends BaseAdapter {

            @Override
            public int getCount() {
                return functionList.size();
            }

            @Override
            public String getItem(int position) {
                return functionList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = View.inflate(mContext, R.layout.item_more_function, null);
                    holder.functionIv = convertView.findViewById(R.id.iv_function);
                    holder.nameTv = convertView.findViewById(R.id.tv_name);
                    holder.bgSl = convertView.findViewById(R.id.sl_bg);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                String functionName = functionList.get(position);
                if ("transmit".equals(functionName)) {
                    // 转发
                    holder.functionIv.setImageResource(currentThemeNo == 3 ? R.drawable.ic_transmit_dark : R.drawable.ic_transmit_light);
                    holder.nameTv.setText(R.string.transmit_to_friends);
                } else if ("download".equals(functionName)) {
                    // 下载
                    holder.functionIv.setImageResource(currentThemeNo == 3 ? R.drawable.ic_download_dark : R.drawable.ic_download_light);
                    holder.nameTv.setText(R.string.save_img);
                } else if ("location".equals(functionName)) {
                    // 定位
                    holder.functionIv.setImageResource(currentThemeNo == 3 ? R.drawable.ic_location_dark : R.drawable.ic_location_light);
                    holder.nameTv.setText(R.string.location_to_chat);
                }
                holder.nameTv.setTextColor(currentThemeNo == 3 ? mContext.getResources().getColor(R.color.color_text_common_level_two) :
                        mContext.getResources().getColor(R.color.color_text_tip));
                holder.bgSl.setBackground(currentThemeNo == 3 ? mContext.getResources().getDrawable(R.drawable.bg_function_corner_dark) :
                        mContext.getResources().getDrawable(R.drawable.bg_function_corner));
                return convertView;
            }

            class ViewHolder {
                private ImageView functionIv;
                private TextView nameTv;
                private SquareLayout bgSl;
                private View itemView;
            }
        }

        private class TransmitAdapter extends BaseAdapter {

            @Override
            public int getCount() {
                return transmitList.size();
            }

            @Override
            public Conversation getItem(int position) {
                return transmitList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = View.inflate(mContext, R.layout.item_more_transmit, null);
                    holder.functionIv = convertView.findViewById(R.id.iv_function);
                    holder.nameTv = convertView.findViewById(R.id.tv_name);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                Context context = convertView.getContext();
                Conversation conversation = transmitList.get(position);
                int defaultIcon = conversation.getType().equals(Conversation.TYPE_GROUP) ?
                        R.drawable.icon_channel_group_default : R.drawable.icon_person_default;
                String imageUrl = CommunicationUtils.getHeadUrl(conversation);
                holder.functionIv.setType(ImageViewRound.TYPE_ROUND);
                holder.functionIv.setRoundRadius(holder.functionIv.dpTodx(10));
                ImageDisplayUtils.getInstance().displayImageByTag(holder.functionIv, imageUrl, defaultIcon);
                holder.nameTv.setText(CommunicationUtils.getName(context, conversation));
                holder.nameTv.setTextColor(currentThemeNo == 3 ? mContext.getResources().getColor(R.color.color_text_common_level_two) :
                        mContext.getResources().getColor(R.color.color_text_tip));
                return convertView;
            }

            class ViewHolder {
                private ImageViewRound functionIv;
                private TextView nameTv;
                private View itemView;
            }
        }

    }

}
