package com.inspur.emmcloud.ui.notsupport;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.ui.IndexActivity;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesByUserUtils;
import com.inspur.emmcloud.util.StringUtils;
import com.inspur.emmcloud.util.UpgradeUtils;

/**
 * 如果有不支持的功能时显示这个界面
 */
public class NotSupportFragment extends Fragment {



    private View rootView;
    private LayoutInflater inflater;
    private TextView unknownFuctionText;
    private TextView titleText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);

        rootView = inflater.inflate(R.layout.fragment_unknown, null);
        unknownFuctionText = (TextView) rootView.findViewById(R.id.app_unknow_text);
        titleText = (TextView) rootView.findViewById(R.id.header_text);
        unknownFuctionText.setText(getClickableSpan());
        unknownFuctionText.setMovementMethod(LinkMovementMethod.getInstance());//必须设置否则无效


    }

    /**
     * 设置标题
     */
    private void setTabTitle(){
        LogUtils.YfcDebug("不支持页面获取到的标题："+((IndexActivity)getActivity()).getNotSupportString());
        String appTabs = PreferencesByUserUtils.getString(getActivity(),"app_tabbar_info_current","");
        if(!StringUtils.isBlank(appTabs)){
            String title = ((IndexActivity)getActivity()).getNotSupportString();
            titleText.setText(title);
        }
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_unknown, container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        setTabTitle();
        return rootView;
    }

    /**
     * 获取spanSgtring
     * @return
     */
    private SpannableString getClickableSpan() {
        View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpgradeUtils upgradeUtils = new UpgradeUtils(getActivity(),handler,false);
                upgradeUtils.checkUpdate(false);
            }
        };

        SpannableString spanableInfo = new SpannableString(
                "当前版本不支持此功能请  立即升级  到最新版本");
        int start = 13;
        int end = 17;
        spanableInfo.setSpan(new Clickable(l), start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spanableInfo.setSpan(new ForegroundColorSpan(0xff0F7BCA),start,end,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanableInfo;
    }

    class Clickable extends ClickableSpan implements View.OnClickListener {
        private final View.OnClickListener mListener;

        public Clickable(View.OnClickListener mListener) {
            this.mListener = mListener;
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }
        @Override
        public void updateDrawState(TextPaint ds) {
            ds.setColor(ds.linkColor);
            ds.setUnderlineText(false);    //去除超链接的下划线
        }
    }

}

