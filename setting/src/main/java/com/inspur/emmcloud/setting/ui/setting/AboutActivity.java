package com.inspur.emmcloud.setting.ui.setting;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.IntentUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.ActionSheetDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.ImageDisplayUtils;
import com.inspur.emmcloud.basemodule.util.LanguageManager;
import com.inspur.emmcloud.basemodule.util.PreferencesByUserAndTanentUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceRouterManager;
import com.inspur.emmcloud.componentservice.app.AppService;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnLongClick;

import static com.inspur.emmcloud.basemodule.util.protocol.ProtocolUtil.PRIVATE_AGREEMENT;
import static com.inspur.emmcloud.basemodule.util.protocol.ProtocolUtil.SERVICE_AGREEMENT;

/**
 * 关于页面 com.inspur.emmcloud.ui.AboutActivity
 *
 * @author Jason Chen; create at 2016年8月23日 下午2:53:14
 */
public class AboutActivity extends BaseActivity {
    private static final int NO_NEED_UPGRADE = 10;
    private static final int UPGRADE_FAIL = 11;
    private static final int DONOT_UPGRADE = 12;
    @BindView(R2.id.tv_app_version)
    TextView appVersionText;
    @BindView(R2.id.iv_logo)
    ImageView logoImg;
    @BindView(R2.id.rl_protocol)
    RelativeLayout protocolLayout;
    @BindView(R2.id.rl_privacy)
    RelativeLayout privacyLayout;
    @BindView(R2.id.rl_invite_friends)
    RelativeLayout inviteFriendsLayout;
    private Handler handler;

    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        String version = AppUtils.getVersion(this).replace("beta", "b");
        appVersionText.setText(AppUtils.getAppName(this) + "  " + version);
        ImageDisplayUtils.getInstance().displayImage(logoImg, "drawable://" + AppUtils.getAppIconRes(BaseApplication.getInstance()), R.drawable.ic_launcher);
        protocolLayout.setVisibility(AppUtils.isAppVersionStandard() ? View.VISIBLE : View.GONE);
        inviteFriendsLayout.setVisibility(AppUtils.isAppVersionStandard() ? View.VISIBLE : View.GONE);
        privacyLayout.setVisibility(AppUtils.isAppVersionStandard() ? View.VISIBLE : View.GONE);
        handMessage();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_about_activity;
    }

    @OnLongClick(R2.id.iv_logo)
    public boolean onLongClick(View v) {
        new ActionSheetDialog.ActionListSheetBuilder(AboutActivity.this)
//						.setTitle(getString(R.string.current_system)+"-->"+ (StringUtils.isBlank(enterpriseName)?getString(R.string.cluster_default):enterpriseName))
                .addItem("idm-->" + WebServiceRouterManager.getInstance().getIDMUrl())
//						.addItem("ecm-->"+ MyApplication.getInstance().getClusterEcm())
                .addItem("emm-->" + WebServiceRouterManager.getInstance().getClusterEmm())
                .addItem("ecm.chat-->" + WebServiceRouterManager.getInstance().getClusterChat())
                .addItem("ecm.schedule-->" + WebServiceRouterManager.getInstance().getClusterSchedule())
                .addItem("ecm.distribution-->" + WebServiceRouterManager.getInstance().getClusterDistribution())
                .addItem("ecm.news-->" + WebServiceRouterManager.getInstance().getClusterNews())
                .addItem("ecm.cloud-drive-->" + WebServiceRouterManager.getInstance().getClusterCloudDrive())
                .addItem("ecm.storage.legacy-->" + WebServiceRouterManager.getInstance().getClusterStorageLegacy())
                .addItem("ecm.client-registry-->" + WebServiceRouterManager.getInstance().getClusterClientRegistry())
                .addItem("ClientId-->" + PreferencesByUserAndTanentUtils.getString(AboutActivity.this, Constant.PREF_CLIENTID, ""))
//						.addItem("DeviceId-->"+ AppUtils.getMyUUID(MyApplication.getInstance()))
                .addItem("DeviceToken-->" + PushManagerUtils.getInstance().getPushId(BaseApplication.getInstance()))
                .build()
                .show();
        return false;
    }

    /**
     * 显示系统信息
     */

    public void onClick(View v) {
        // TODO Auto-generated method stub
        int id = v.getId();
        if (id == R.id.ibt_back) {
            finish();
        } else if (id == R.id.rl_welcome) {
            Bundle bundle = new Bundle();
            bundle.putString("from", "about");
            IntentUtils.startActivity(AboutActivity.this, GuideActivity.class,
                    bundle);
        } else if (id == R.id.rl_protocol) {
//            IntentUtils.startActivity(AboutActivity.this, ServiceTermActivity.class);
            openProtocol(getString(R.string.setting_about_protocol_text),SERVICE_AGREEMENT + LanguageManager.getInstance().getCurrentAppLanguage() + ".html");
        } else if (id == R.id.rl_check_update) {
            AppService appService = Router.getInstance().getService(AppService.class);
            if (appService != null) {
                appService.checkAppUpdate(this, true, handler);
            }
        } else if (id == R.id.rl_invite_friends) {
            IntentUtils.startActivity(AboutActivity.this, RecommendAppActivity.class);
        } else if (id == R.id.rl_privacy) {
            openProtocol(getString(R.string.setting_about_privacy_text), PRIVATE_AGREEMENT);
        }
    }


    private void openProtocol(String appName,String uri){
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        LogUtils.jasonDebug("uri===" + uri);
        bundle.putString("appName", appName);
//        bundle.putBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, isHaveNavBar);
        ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();
    }

    private void handMessage() {
        // TODO Auto-generated method stub
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                // TODO Auto-generated method stub
                switch (msg.what) {
                    case NO_NEED_UPGRADE:
                    case UPGRADE_FAIL:
                        ToastUtils.show(getApplicationContext(), R.string.app_is_lastest_version);
                        break;
                    case DONOT_UPGRADE:
                        break;
                    default:
                        break;
                }
            }
        };
    }

}
