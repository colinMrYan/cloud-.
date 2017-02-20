package com.inspur.emmcloud.ui.mine;

import java.io.Serializable;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.bean.GetMyInfoResult;
import com.inspur.emmcloud.ui.find.MyReactFindActivity;
import com.inspur.emmcloud.ui.find.RNActivity;
import com.inspur.emmcloud.ui.mine.cardpackage.CardPackageListActivity;
import com.inspur.emmcloud.ui.mine.feedback.FeedBackActivity;
import com.inspur.emmcloud.ui.mine.myinfo.MyInfoActivity;
import com.inspur.emmcloud.ui.mine.setting.SettingActivity;
import com.inspur.emmcloud.util.ImageDisplayUtils;
import com.inspur.emmcloud.util.LogUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.UriUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

/**
 * 更多页面
 */
public class MoreFragment extends Fragment {

    private static final int UPDATE_MY_HEAD = 3;
    private static final String ACTION_NAME = "userInfo";

    public static Handler handler;

    private View rootView;
    private LayoutInflater inflater;
    private LoadingDialog loadingDlg;
    private RelativeLayout setContentItem;
    private RelativeLayout userHeadLayout;
    private ImageView moreHeadImg;
    private TextView userNameText;
    private TextView userOrgText;
    private ImageView userCodeImg;
    private ImageDisplayUtils imageDisplayUtils;
    private GetMyInfoResult getMyInfoResult;
    private String userheadUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        inflater = (LayoutInflater) getActivity().getSystemService(
                getActivity().LAYOUT_INFLATER_SERVICE);

        rootView = inflater.inflate(R.layout.fragment_mine, null);
        loadingDlg = new LoadingDialog(getActivity());

        handMessage();


        setContentItem = (RelativeLayout) rootView.findViewById(R.id.more_set_layout);
        userHeadLayout = (RelativeLayout) rootView.findViewById(R.id.more_userhead_layout);
        setContentItem.setOnClickListener(onClickListener);
        userHeadLayout.setOnClickListener(onClickListener);
        ((RelativeLayout) rootView.findViewById(R.id.more_help_layout)).setOnClickListener(onClickListener);
        ((RelativeLayout) rootView.findViewById(R.id.more_message_layout)).setOnClickListener(onClickListener);
        ((RelativeLayout) rootView.findViewById(R.id.more_invite_friends_layout)).setOnClickListener(onClickListener);
        ((RelativeLayout) rootView.findViewById(R.id.more_cardpackage_layout)).setOnClickListener(onClickListener);
        ((RelativeLayout) rootView.findViewById(R.id.more_department_layout)).setOnClickListener(onClickListener);
        moreHeadImg = (ImageView) rootView.findViewById(R.id.more_head_img);
        userNameText = (TextView) rootView.findViewById(R.id.more_head_textup);
        userOrgText = (TextView) rootView.findViewById(R.id.more_head_textdown);
        userCodeImg = (ImageView) rootView.findViewById(R.id.more_head_codeImg);

        imageDisplayUtils = new ImageDisplayUtils(getActivity(), R.drawable.icon_photo_default);
        getMyInfo();
    }


    private void getMyInfo() {
        // TODO Auto-generated method stub
        String myInfo = PreferencesUtils.getString(getActivity(), "myInfo", "");
        getMyInfoResult = new GetMyInfoResult(myInfo);
        String inspurId = getMyInfoResult.getID();
        String photoUri = UriUtils.getChannelImgUri(inspurId);
        imageDisplayUtils.display(moreHeadImg, photoUri);

        LogUtils.debug("yfcLofg", "more里面的" + photoUri);
        if (!getMyInfoResult.getName().equals("null")) {
            userNameText.setText(getMyInfoResult.getName());
        } else {
            userNameText.setText(getString(R.string.not_set));
        }

        userOrgText.setText(getMyInfoResult.getEnterpriseName());
    }


    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case UPDATE_MY_HEAD:
                        getMyInfoResult.setAvatar((String) msg.obj);
                        userheadUrl = "https://mob.inspur.com" + getMyInfoResult.getAvatar();
                        imageDisplayUtils.display(moreHeadImg, userheadUrl);
                        break;
                    default:
                        break;
                }
            }

        };
    }

    private void showDialog() {
        View view = getActivity().getLayoutInflater().inflate(R.layout.mine_team_choose_dialog, null);
        Dialog dialog = new Dialog(getActivity(), R.style.transparentFrameWindowStyle);
        dialog.setContentView(view, new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
        Window window = dialog.getWindow();
        // 设置显示动画
        window.setWindowAnimations(R.style.main_menu_animstyle);
        WindowManager.LayoutParams wl = window.getAttributes();
        wl.x = 0;
        wl.y = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        // 以下这两句是为了保证按钮可以水平满屏
        wl.width = LayoutParams.MATCH_PARENT;
        wl.height = LayoutParams.WRAP_CONTENT;

        // 设置显示位置
        dialog.onWindowAttributesChanged(wl);
        // 设置点击外围解散
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private OnClickListener onClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            Intent intent = new Intent();
            switch (v.getId()) {
                case R.id.more_set_layout:
                    intent.setClass(getActivity(), SettingActivity.class);
                    startActivity(intent);
                    break;
                case R.id.more_userhead_layout:
                    intent.setClass(getActivity(), MyInfoActivity.class);
                    intent.putExtra("getMyInfoResult", (Serializable) getMyInfoResult);
                    startActivity(intent);
                    break;
                case R.id.more_help_layout:
				String feedbackUrl ="http://uservoices.inspur.com/feedback/";
				intent.setClass(getActivity(), FeedBackActivity.class);
				startActivity(intent);

//				ComponentName componentName = new ComponentName(
//			            "com.myprojectone",
//			            "com.myprojectone.MainActivity");
//			        Intent intentOpen = new Intent();
//			        Bundle bundle = new Bundle();
//			        bundle.putString("resUrl", resurl);
//			        bundle.putSerializable("picUrlList", picurllist);
//			        intentOpen.putExtras(bundle);
//			        intentOpen.setComponent(componentName);
//			        startActivity(intentOpen);
//                    in(v);

//				UriUtils.open(getActivity(), feedbackUrl, 3, "", getString(R.string.more_help));
                    break;
                case R.id.more_message_layout:
                case R.id.more_invite_friends_layout:
                    ToastUtils.show(getActivity(), R.string.function_not_implemented);
                    break;
                case R.id.more_cardpackage_layout:
                    intent.setClass(getActivity(), CardPackageListActivity.class);
                    startActivity(intent);
                    break;
                case R.id.more_department_layout:
                    showDialog();
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_mine, container,
                    false);
        }

        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    public void in(View v) {
//		ComponentName componentName = new ComponentName(
//	            "com.myprojectone",
//	            "com.myprojectone.MainActivity");
//		ComponentName componentName = new ComponentName(
//	            "com.myproject",
//	            "com.myproject.MainActivity");
//		ComponentName componentName = new ComponentName(
//	            "com.example.androidtest",
//	            "com.example.androidtest.MainActivity");
        Intent intentOpen = new Intent();
        intentOpen.setClass(getActivity(), RNActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("resUrl", "resUrl");
        bundle.putSerializable("picUrlList", new ArrayList<String>());
        intentOpen.putExtras(bundle);
//	        intentOpen.setComponent(componentName);
//	        intentOpen.putExtra("tabs", "11111");
//	        intentOpen.putExtra("data", bundle);
//	        {\"title\":\"communicate\",\"selected\":false}
//	        intentOpen.setData(Uri.parse("ecm-contact://9104"));
        startActivity(intentOpen);
        getActivity().overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_zoom_out);
    }

    public void out(View v) {
        getActivity().overridePendingTransition(R.anim.anim_zoom_in, R.anim.anim_zoom_out);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    public class WebService extends APIInterfaceInstance {


    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (handler != null) {
            handler = null;
        }
    }
}
