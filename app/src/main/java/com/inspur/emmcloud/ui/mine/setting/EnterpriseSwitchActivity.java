package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MainActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.adapter.EnterpriseAdapter;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;
import com.inspur.emmcloud.util.privates.PushManagerUtils;
import com.inspur.emmcloud.widget.ScrollViewWithListView;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;

import java.util.List;

/**
 * Created by Administrator on 2017/5/25.
 */

@ContentView(R.layout.activity_mine_enterprise_switch)
public class EnterpriseSwitchActivity extends BaseActivity {

    @ViewInject(R.id.lv_enterprise)
    private ScrollViewWithListView enterpriseListView;
    @ViewInject(R.id.rl_setting_close_auto_select)
    private RelativeLayout closeAutoSelectLayout;
    private GetMyInfoResult getMyInfoResult;
    private List<Enterprise> enterpriseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getEnterpriseList();
        initView();
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
        new MyQMUIDialog.MessageDialogBuilder(EnterpriseSwitchActivity.this)
                .setMessage(getString(R.string.sure_switch_to, enterprise.getName()))
                .addAction(R.string.cancel, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
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
        WebSocketPush.getInstance().closeWebsocket();
        PreferencesByUsersUtils.putString(getApplicationContext(), Constant.PREF_CURRENT_ENTERPRISE_ID, enterprise.getId());
        MyApplication.getInstance().initTanent();
        PushManagerUtils.getInstance().stopPush();
        MyApplication.getInstance().clearNotification();
        MyApplication.getInstance().removeAllCookie();
        MyApplication.getInstance().clearUserPhotoMap();
        PreferencesUtils.putBoolean(EnterpriseSwitchActivity.this, "isMDMStatusPass", false);
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
