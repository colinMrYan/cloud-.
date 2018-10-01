package com.inspur.emmcloud.ui.notsupport;


import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Html;
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
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.AppTabUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.util.privates.UpgradeUtils;

/**
 * 如果有不支持的功能时显示这个界面
 */
public class NotSupportFragment extends Fragment {


    private static final int NO_NEED_UPGRADE = 10;
    private static final int UPGRADE_FAIL = 11;
    private static final int DONOT_UPGRADE = 12;
    private View rootView;
    private LayoutInflater inflater;
    private TextView unknownFuctionText;
    private String currentFragmentheader;
    private String secondPartContant="";
    private String endPartContent="";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);

        rootView = inflater.inflate(R.layout.fragment_unknown, null);
        unknownFuctionText = (TextView) rootView.findViewById(R.id.app_unknow_text);
        setTabTitle();
        //应用功能已改版，请升级到最新版本
        unknownFuctionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpgradeUtils upgradeUtils = new UpgradeUtils(getActivity(),handler,false);
                upgradeUtils.checkUpdate(false);
            }
        });
        getNoSupportContent(AppUtils.getCurrentAppLanguage(getContext()));
      unknownFuctionText.setText(Html.fromHtml(currentFragmentheader+secondPartContant+"<font color='#0F7BCA'>"+endPartContent+"</font>"));

        unknownFuctionText.setMovementMethod(LinkMovementMethod.getInstance());//必须设置否则无效

    }

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
        return rootView;
    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NO_NEED_UPGRADE:
                    ToastUtils.show(getActivity(), R.string.app_is_lastest_version);
                    break;
                case UPGRADE_FAIL:
                    ToastUtils.show(getActivity(), R.string.check_update_fail);
                    break;
                case DONOT_UPGRADE:
                    break;
                default:
                    break;
            }
        }
    };

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

    /**
     * 设置标题，根据当前Fragment类名获取显示名称
     */
    private void setTabTitle() {
        String uri = "";
        if (!StringUtils.isBlank(getArguments().getString("uri"))){
            uri = getArguments().getString("uri");
            String appTabs = PreferencesByUserAndTanentUtils.getString(getActivity(), Constant.PREF_APP_TAB_BAR_INFO_CURRENT, "");
            if (!StringUtils.isBlank(appTabs)) {

                currentFragmentheader  =  AppTabUtils.getTabTitle(getActivity(),NotSupportFragment.class.getSimpleName(),uri);
                ((TextView) rootView.findViewById(R.id.header_text)).setText(currentFragmentheader);
            }
        }

    }

    /**
     *
     * */
    private void getNoSupportContent(String environmentLanguage) {
        switch (environmentLanguage.toLowerCase()) {
            case "zh-hant":
                secondPartContant = "已改版，請";
                endPartContent    = "升級到最新版本";
                break;
            case "en":
            case "en-us":
                secondPartContant = "Revised, Please ";
                endPartContent    = "upgrade to the latest version";
                break;
            default:
                secondPartContant = "已改版, 请";
                endPartContent    = "升级到最新版本";
                break;
        }
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

