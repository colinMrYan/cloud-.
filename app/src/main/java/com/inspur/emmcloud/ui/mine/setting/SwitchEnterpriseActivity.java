package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MainActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.bean.mine.Enterprise;
import com.inspur.emmcloud.bean.mine.GetMyInfoResult;
import com.inspur.emmcloud.config.Constant;
import com.inspur.emmcloud.push.WebSocketPush;
import com.inspur.emmcloud.util.common.PreferencesUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.emmcloud.util.common.ToastUtils;
import com.inspur.emmcloud.util.privates.PreferencesByUsersUtils;
import com.inspur.emmcloud.widget.ScrollViewWithListView;
import com.inspur.emmcloud.widget.dialogs.MyQMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.util.List;

/**
 * Created by Administrator on 2017/5/25.
 */

public class SwitchEnterpriseActivity extends BaseActivity {
    private List<Enterprise> enterpriseList;
    private ScrollViewWithListView enterpriseListView;
    private GetMyInfoResult getMyInfoResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_switch_enterprise);
        getEnterpriseList();
        initView();
    }

    private void getEnterpriseList() {
        String myInfo = PreferencesUtils.getString(this, "myInfo", "");
        getMyInfoResult = new GetMyInfoResult(myInfo);
        enterpriseList = getMyInfoResult.getEnterpriseList();
    }

    private void initView() {
        String selectLoginEnterpriseId= PreferencesByUsersUtils.getString(this, Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID,"");
        if(!StringUtils.isBlank(selectLoginEnterpriseId)){
            findViewById(R.id.clear_auto_select_enterprise_layout).setVisibility(View.VISIBLE);
        }
        ((TextView) findViewById(R.id.header_text)).setText(R.string.select_enterprise);
        enterpriseListView = (ScrollViewWithListView) findViewById(R.id.device_list);
        enterpriseListView.setAdapter(adapter);
        enterpriseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Enterprise enterprise = enterpriseList.get(position);
                if (!enterprise.getId().equals(((MyApplication) getApplicationContext()).getCurrentEnterprise().getId())) {
                    showSwitchPromptDlg(enterprise);
                }
            }
        });

    }

    /**
     * 弹出租户切换提示框
     *
     * @param enterprise
     */
    private void showSwitchPromptDlg(final Enterprise enterprise) {
        new MyQMUIDialog.MessageDialogBuilder(SwitchEnterpriseActivity.this)
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
        PreferencesByUsersUtils.putString(getApplicationContext(), "current_enterprise_id", enterprise.getId());
        ((MyApplication) getApplicationContext()).initTanent();
        ((MyApplication) getApplicationContext()).stopPush();
        ((MyApplication) getApplicationContext()).clearNotification();
        ((MyApplication) getApplicationContext()).removeAllCookie();
        ((MyApplication) getApplicationContext()).clearUserPhotoMap();
        PreferencesUtils.putBoolean(SwitchEnterpriseActivity.this, "isMDMStatusPass", false);
        Intent intent = new Intent(SwitchEnterpriseActivity.this,
                MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.clear_auto_select_enterprise_layout:
                v.setVisibility(View.GONE);
                PreferencesByUsersUtils.putString(this, Constant.PREF_SELECT_LOGIN_ENTERPRISE_ID,"");
                ToastUtils.show(MyApplication.getInstance(),R.string.turn_off_success);
                break;
            default:
                break;
        }
    }

    private BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return enterpriseList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Enterprise enterprise = enterpriseList.get(position);
            convertView = LayoutInflater.from(SwitchEnterpriseActivity.this).inflate(R.layout.mine_setting_enterprise_item_view, null);
            ((TextView) convertView.findViewById(R.id.enterprise_text)).setText(enterprise.getName());
            if (enterprise.getId().equals(((MyApplication) getApplicationContext()).getCurrentEnterprise().getId())) {
                (convertView.findViewById(R.id.current_enterprise_text)).setVisibility(View.VISIBLE);
                (convertView.findViewById(R.id.img)).setVisibility(View.INVISIBLE);
            }
            return convertView;
        }
    };
}
