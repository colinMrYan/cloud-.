package com.inspur.emmcloud.setting.ui.setting;

import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.router.Router;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.ScrollViewWithListView;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.bean.Enterprise;
import com.inspur.emmcloud.basemodule.bean.GetMyInfoResult;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.push.PushManagerUtils;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils;
import com.inspur.emmcloud.componentservice.communication.CommunicationService;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.R2;
import com.inspur.emmcloud.setting.adapter.EnterpriseAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/5/25.
 */

public class EnterpriseSwitchActivity extends BaseActivity {

    @BindView(R2.id.lv_enterprise)
    ScrollViewWithListView enterpriseListView;
    @BindView(R2.id.rl_setting_close_auto_select)
    RelativeLayout closeAutoSelectLayout;
    private GetMyInfoResult getMyInfoResult;
    private List<Enterprise> enterpriseList;


    @Override
    public void onCreate() {
        ButterKnife.bind(this);
        getEnterpriseList();
        initView();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_mine_enterprise_switch_activity;
    }

    private void getEnterpriseList() {
        String myInfo = PreferencesUtils.getString(this, "myInfo", "");
        getMyInfoResult = new GetMyInfoResult(myInfo);
        enterpriseList = getMyInfoResult.getEnterpriseList();
    }

    private void initView() {
        String selectLoginEnterpriseId = PreferencesByUsersUtils.getString(this, Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID, "");
        if (!StringUtils.isBlank(selectLoginEnterpriseId)) {
            closeAutoSelectLayout.setVisibility(View.VISIBLE);
        }
        enterpriseListView.setAdapter(new EnterpriseAdapter(this, enterpriseList));
        enterpriseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Enterprise enterprise = enterpriseList.get(position);
                if (!enterprise.getId().equals(BaseApplication.getInstance().getCurrentEnterprise().getId())) {
                    showSwitchEnterpriseConfirmDlg(enterprise);
                }
            }
        });

    }

    /**
     * 弹出租户切换提示框
     *
     * @param enterprise
     */
    private void showSwitchEnterpriseConfirmDlg(final Enterprise enterprise) {
        new CustomDialog.MessageDialogBuilder(EnterpriseSwitchActivity.this)
                .setMessage(getString(R.string.setting_sure_switch_to, enterprise.getName()))
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switchToEnterprise(enterprise);
                    }
                })
                .show();
    }

    /**
     * 切换企业信息
     *
     * @param enterprise
     */
    private void switchToEnterprise(Enterprise enterprise) {
        CommunicationService communicationService = Router.getInstance().getService(CommunicationService.class);
        if (communicationService != null) {
            communicationService.closeWebSocket();
        }
        PreferencesByUsersUtils.putString(getApplicationContext(), Constant.PREF_CURRENT_ENTERPRISE_ID, enterprise.getId());
        BaseApplication.getInstance().initTanent();
        PushManagerUtils.getInstance().stopPush();
        BaseApplication.getInstance().clearNotification();
        BaseApplication.getInstance().removeAllCookie();
        BaseApplication.getInstance().clearUserPhotoMap();
        PreferencesUtils.putBoolean(EnterpriseSwitchActivity.this, Constant.PREF_MDM_STATUS_PASS, false);
//        Intent intent = new Intent(EnterpriseSwitchActivity.this,
//                MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        Bundle bundle =new Bundle();
        ARouter.getInstance().build(Constant.AROUTER_CLASS_APP_MAIN).withFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK).navigation(this);
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ibt_back) {
            finish();
        } else if (id == R.id.rl_setting_close_auto_select) {
            v.setVisibility(View.GONE);
            PreferencesByUsersUtils.putString(this, Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID, "");
            ToastUtils.show(BaseApplication.getInstance(), R.string.turn_off_success);
        }
    }


}
