package com.inspur.emmcloud.setting.ui.setting;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.baselib.widget.ScrollViewWithListView;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.application.BaseApplication;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.api.SettingAPIInterfaceImpl;
import com.inspur.emmcloud.setting.api.SettingAPIService;
import com.inspur.emmcloud.setting.bean.BindingDevice;
import com.inspur.emmcloud.setting.bean.BindingDeviceLog;
import com.inspur.emmcloud.setting.bean.GetDeviceLogResult;

import java.util.List;

/**
 * Created by Administrator on 2017/5/15.
 */

public class DeviceInfoActivity extends BaseActivity {

    private BindingDevice bindingDevice;
    private LoadingDialog loadingDialog;
    private ScrollViewWithListView deviceLogListView;
    private SettingAPIService apiService;

    @Override
    public void onCreate() {
        loadingDialog = new LoadingDialog(this);
        bindingDevice = (BindingDevice) getIntent().getSerializableExtra("binding_device");
        ((TextView) findViewById(R.id.device_model_text)).setText(bindingDevice.getDeviceModel());
        ((TextView) findViewById(R.id.device_id_text)).setText(bindingDevice.getDeviceId());
        if (StringUtils.isEmpty(bindingDevice.getDeviceVersion())) {
            ((RelativeLayout) findViewById(R.id.device_version_container)).setVisibility(View.GONE);
        } else {
            ((RelativeLayout) findViewById(R.id.device_version_container)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.device_version_text)).setText(bindingDevice.getDeviceVersion());
        }
        String deviceLastUserTime = TimeUtils.getTime(bindingDevice.getDeviceLastUserTime(), TimeUtils.getFormat(DeviceInfoActivity.this, TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
        ((TextView) findViewById(R.id.device_last_use_time_text)).setText(deviceLastUserTime);
        if (getIntent().getBooleanExtra("isCurrentBind", false)) {
            findViewById(R.id.bt_device_unbound).setVisibility(View.VISIBLE);
        }
        deviceLogListView = (ScrollViewWithListView) findViewById(R.id.device_log_list);
        apiService = new SettingAPIService(this);
        apiService.setAPIInterface(new WebService());
        getDeviceLog();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_device_info_activity;
    }

    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.ibt_back) {
            finish();

        } else if (i == R.id.bt_device_unbound) {
            showUnbindDevicePromptDlg();

        } else if (i == R.id.device_id_text) {
            ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cmb.setPrimaryClip(ClipData.newPlainText(null, bindingDevice.getDeviceId()));
            ToastUtils.show(this, R.string.copyed_to_paste_board);

        } else {
        }
    }

    /**
     * 弹出解绑设备提示框
     */
    private void showUnbindDevicePromptDlg() {
        String warningText = bindingDevice.getDeviceId().equals(AppUtils.getMyUUID(getApplicationContext())) ? getString(R.string.setting_device_current_unbind_warning) : getString(R.string.setting_device_other_unbind_warning, bindingDevice.getDeviceModel());
        new CustomDialog.MessageDialogBuilder(DeviceInfoActivity.this)
                .setMessage(warningText)
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
                        unbindDevice();
                    }
                })
                .show();
    }

    private void getDeviceLog() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDialog.show();
            apiService.getDeviceLogList(bindingDevice.getDeviceId());
        }
    }

    /**
     * 解绑设备
     */
    private void unbindDevice() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDialog.show();
            apiService.unBindDevice(bindingDevice.getDeviceId());
        }
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
            convertView = LayoutInflater.from(DeviceInfoActivity.this).inflate(R.layout.setting_item_view_device_log, null);
            BindingDeviceLog log = bindingDeviceLogList.get(position);
            String time = TimeUtils.getTime(log.getTime(), TimeUtils.getFormat(DeviceInfoActivity.this, TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
            ((TextView) convertView.findViewById(R.id.time_text)).setText(time);
            ((TextView) convertView.findViewById(R.id.log_text)).setText(log.getDesc());
            return convertView;
        }
    }

    private class WebService extends SettingAPIInterfaceImpl {
        @Override
        public void returnUnBindDeviceSuccess() {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            ToastUtils.show(getApplicationContext(), R.string.setting_device_unbind_sucess);
            if (bindingDevice.getDeviceId().equals(AppUtils.getMyUUID(getApplicationContext()))) {
                ((BaseApplication) getApplication()).signout();
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

        @Override
        public void returnDeviceLogListSuccess(GetDeviceLogResult getDeviceLogResult) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            List<BindingDeviceLog> bindingDeviceLogList = getDeviceLogResult.getBindingDeviceLogList();
            if (bindingDeviceLogList.size() > 0) {
                (findViewById(R.id.history_text)).setVisibility(View.VISIBLE);
            }

            deviceLogListView.setAdapter(new Adapter(bindingDeviceLogList));
        }

        @Override
        public void returnDeviceLogListFail(String error, int errorCode) {
            if (loadingDialog != null && loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            WebServiceMiddleUtils.hand(getApplicationContext(), error, errorCode);
        }
    }


}
