package com.inspur.emmcloud.ui.mine.setting;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.BindingDevice;
import com.inspur.emmcloud.bean.BindingDeviceLog;
import com.inspur.emmcloud.ui.login.LoginActivity;
import com.inspur.emmcloud.util.AppUtils;
import com.inspur.emmcloud.util.NetUtils;
import com.inspur.emmcloud.util.PreferencesUtils;
import com.inspur.emmcloud.util.TimeUtils;
import com.inspur.emmcloud.util.ToastUtils;
import com.inspur.emmcloud.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.ScrollViewWithListView;
import com.inspur.emmcloud.widget.dialogs.EasyDialog;

import java.util.List;

import cn.jpush.android.api.JPushInterface;

/**
 * Created by Administrator on 2017/5/15.
 */

public class DeviceInfoActivity extends BaseActivity {

    private BindingDevice bindingDevice;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_device_info);
        loadingDialog = new LoadingDialog(this);
        bindingDevice = (BindingDevice) getIntent().getSerializableExtra("binding_device");
        ((TextView) findViewById(R.id.device_model_text)).setText(bindingDevice.getDeviceModel());
        ((TextView) findViewById(R.id.device_id_text)).setText(bindingDevice.getDeviceId());
        String bindingTime = TimeUtils.getTime(bindingDevice.getDeviceBindTime(), TimeUtils.getFormat(DeviceInfoActivity.this, TimeUtils.FORMAT_DEFAULT_DATE));
        ((TextView) findViewById(R.id.device_bind_time_text)).setText(bindingTime);
        if (getIntent().getBooleanExtra("isCurrentBind", false)) {
            findViewById(R.id.device_unbound_btn).setVisibility(View.VISIBLE);
        }
        List<BindingDeviceLog> bindingDeviceLogList = bindingDevice.getBindingDeviceLogList();
        if (bindingDeviceLogList.size() > 0) {
            (findViewById(R.id.history_text)).setVisibility(View.VISIBLE);
        }
        ScrollViewWithListView deviceLogListView = (ScrollViewWithListView) findViewById(R.id.device_log_list);
        deviceLogListView.setAdapter(new Adapter(bindingDeviceLogList));

    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_layout:
                finish();
                break;
            case R.id.device_unbound_btn:
                showUnbindDeviceWarningDlg();
                break;
            case R.id.device_id_text:
                ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                cmb.setPrimaryClip(ClipData.newPlainText(null, bindingDevice.getDeviceId()));
                ToastUtils.show(this, R.string.copyed_to_paste_board);
                break;
            default:
                break;
        }
    }

    private void showUnbindDeviceWarningDlg() {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == -1) {
                    unbindDevice();
                }
                dialog.dismiss();
            }
        };
        String warningText = bindingDevice.getDeviceId().equals(AppUtils.getMyUUID(getApplicationContext())) ? getString(R.string.device_current_unbind_warning) : getString(R.string.device_other_unbind_warning, bindingDevice.getDeviceModel());
        EasyDialog.showDialog(DeviceInfoActivity.this,
                getString(R.string.prompt),
                warningText,
                getString(R.string.ok), getString(R.string.cancel),
                listener, true);
    }


    /**
     * 注销登录
     */
    private void signout() {
        // TDO Auto-generated method stub
        if (((MyApplication) getApplicationContext()).getWebSocketPush() != null) {
            ((MyApplication) getApplicationContext()).getWebSocketPush()
                    .webSocketSignout();
        }
        ((MyApplication) getApplicationContext()).clearNotification();
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        ((MyApplication) getApplicationContext()).removeAllCookie();
        JPushInterface.stopPush(getApplicationContext());
        PreferencesUtils.putString(DeviceInfoActivity.this, "tokenType", "");
        PreferencesUtils.putString(DeviceInfoActivity.this, "accessToken", "");
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.setClass(this, LoginActivity.class);
        startActivity(intent);
        this.finish();
    }

    private class Adapter extends BaseAdapter {
        private List<BindingDeviceLog> bindingDeviceLogList;

        public Adapter(List<BindingDeviceLog> bindingDeviceLogList) {
            this.bindingDeviceLogList = bindingDeviceLogList;
        }

        @Override
        public int getCount() {
            return bindingDeviceLogList.size();
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
            convertView = LayoutInflater.from(DeviceInfoActivity.this).inflate(R.layout.item_view_device_log, null);
            BindingDeviceLog log = bindingDeviceLogList.get(position);
            String time = TimeUtils.getTime(log.getTime(), TimeUtils.getFormat(DeviceInfoActivity.this, TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
            ((TextView) convertView.findViewById(R.id.time_text)).setText(time);
            ((TextView) convertView.findViewById(R.id.log_text)).setText(log.getDesc());
            return convertView;
        }
    }

    private void unbindDevice() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDialog.show();
            MineAPIService apiService = new MineAPIService(this);
            apiService.setAPIInterface(new WebService());
            apiService.unBindDevice(bindingDevice.getDeviceId());
        }
    }

    private class WebService extends APIInterfaceInstance {
        @Override
        public void returnUnBindDeviceSuccess() {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            ToastUtils.show(getApplicationContext(), R.string.device_unbind_sucess);
            if (bindingDevice.getDeviceId().equals(AppUtils.getMyUUID(getApplicationContext()))) {
                signout();
            } else {
                setResult(RESULT_OK, getIntent());
            }
            finish();
        }

        @Override
        public void returnUnBindDeviceFail(String error, int errorCode) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }
}
