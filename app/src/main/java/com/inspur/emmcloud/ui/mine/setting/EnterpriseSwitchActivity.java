package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MainActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.EnterpriseAdapter;
import com.inspur.emmcloud.baselib.util.PreferencesUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;
import com.inspur.emmcloud.util.privates.PushManagerUtils;
import com.inspur.emmcloud.widget.ScrollViewWithListView;
import com.inspur.emmcloud.widget.dialogs.CustomDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Administrator on 2017/5/25.
 */

public class EnterpriseSwitchActivity extends BaseActivity {

    @BindView(R.id.lv_enterprise)
    ScrollViewWithListView enterpriseListView;
    @BindView(R.id.rl_setting_close_auto_select)
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
        return R.layout.activity_mine_enterprise_switch;
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
                if (!enterprise.getId().equals(MyApplication.getInstance().getCurrentEnterprise().getId())) {
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
                .setMessage(getString(R.string.sure_switch_to, enterprise.getName()))
                .setNegativeButton(R.string.cancel, (dialog, index) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.ok, (dialog, index) -> {
                    dialog.dismiss();
                    switchToEnterprise(enterprise);
                })
                .show();
    }

    /**
     * 切换企业信息
     *
     * @param enterprise
     */
    private void switchToEnterprise(Enterprise enterprise) {
        WebSocketPush.getInstance().closeWebsocket();
        PreferencesByUsersUtils.putString(getApplicationContext(), Constant.PREF_CURRENT_ENTERPRISE_ID, enterprise.getId());
        MyApplication.getInstance().initTanent();
        PushManagerUtils.getInstance().stopPush();
        MyApplication.getInstance().clearNotification();
        MyApplication.getInstance().removeAllCookie();
        MyApplication.getInstance().clearUserPhotoMap();
        PreferencesUtils.putBoolean(EnterpriseSwitchActivity.this, Constant.PREF_MDM_STATUS_PASS, false);
        Intent intent = new Intent(EnterpriseSwitchActivity.this,
                MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.rl_setting_close_auto_select:
                v.setVisibility(View.GONE);
                PreferencesByUsersUtils.putString(this, Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID, "");
                ToastUtils.show(MyApplication.getInstance(), R.string.turn_off_success);
                break;
            default:
                break;
        }
    }


}
