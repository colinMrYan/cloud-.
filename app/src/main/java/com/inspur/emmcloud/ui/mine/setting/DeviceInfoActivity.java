package com.inspur.emmcloud.ui.mine.setting;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.MyApplication;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.bean.mine.BindingDevice;
import com.inspur.emmcloud.bean.mine.BindingDeviceLog;
import com.inspur.emmcloud.bean.mine.GetDeviceLogResult;
import com.inspur.emmcloud.util.privates.NetUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;
import com.inspur.emmcloud.widget.ScrollViewWithListView;
import com.inspur.emmcloud.widget.dialogs.CustomDialog;

import java.util.List;

/**
 * Created by Administrator on 2017/5/15.
 */

public class DeviceInfoActivity extends BaseActivity {

    private BindingDevice bindingDevice;
    private LoadingDialog loadingDialog;
    private ScrollViewWithListView deviceLogListView;
    private MineAPIService apiService;

    @Override
    public void onCreate() {
        loadingDialog = new LoadingDialog(this);
        bindingDevice = (BindingDevice) getIntent().getSerializableExtra("binding_device");
        ((TextView) findViewById(R.id.device_model_text)).setText(bindingDevice.getDeviceModel());
        ((TextView) findViewById(R.id.device_id_text)).setText(bindingDevice.getDeviceId());
        String deviceLastUserTime = TimeUtils.getTime(bindingDevice.getDeviceLastUserTime(), TimeUtils.getFormat(DeviceInfoActivity.this, TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
        ((TextView) findViewById(R.id.device_last_use_time_text)).setText(deviceLastUserTime);
        if (getIntent().getBooleanExtra("isCurrentBind", false)) {
            findViewById(R.id.bt_device_unbound).setVisibility(View.VISIBLE);
        }
        deviceLogListView = (ScrollViewWithListView) findViewById(R.id.device_log_list);
        apiService = new MineAPIService(this);
        apiService.setAPIInterface(new WebService());
        getDeviceLog();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_setting_device_info;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            case R.id.bt_device_unbound:
                showUnbindDevicePromptDlg();
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

    /**
     * 弹出解绑设备提示框
     */
    private void showUnbindDevicePromptDlg() {
        String warningText = bindingDevice.getDeviceId().equals(AppUtils.getMyUUID(getApplicationContext())) ? getString(R.string.device_current_unbind_warning) : getString(R.string.device_other_unbind_warning, bindingDevice.getDeviceModel());
        new CustomDialog.MessageDialogBuilder(DeviceInfoActivity.this)
                .setMessage(warningText)
                .setNegativeButton(R.string.cancel, (dialog, index) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.ok, (dialog, index) -> {
                    dialog.dismiss();
                    unbindDevice();
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
            convertView = LayoutInflater.from(DeviceInfoActivity.this).inflate(R.layout.item_view_device_log, null);
            BindingDeviceLog log = bindingDeviceLogList.get(position);
            String time = TimeUtils.getTime(log.getTime(), TimeUtils.getFormat(DeviceInfoActivity.this, TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));
            ((TextView) convertView.findViewById(R.id.time_text)).setText(time);
            ((TextView) convertView.findViewById(R.id.log_text)).setText(log.getDesc());
            return convertView;
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
                ((MyApplication) getApplication()).signout();
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
