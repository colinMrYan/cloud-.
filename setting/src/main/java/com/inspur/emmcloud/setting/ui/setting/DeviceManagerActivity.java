package com.inspur.emmcloud.setting.ui.setting;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.baselib.util.TimeUtils;
import com.inspur.emmcloud.baselib.widget.LoadingDialog;
import com.inspur.emmcloud.basemodule.ui.BaseActivity;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.setting.R;
import com.inspur.emmcloud.setting.api.SettingAPIInterfaceImpl;
import com.inspur.emmcloud.setting.api.SettingAPIService;
import com.inspur.emmcloud.setting.bean.BindingDevice;
import com.inspur.emmcloud.setting.bean.GetBindingDeviceResult;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/5/15.
 */

public class DeviceManagerActivity extends BaseActivity {

    private final static int UNBIND_DEVICE = 1;
    private ListView currentDeviceListView, historyDeviceListView;
    private Adapter currentDeviceAdapter, historyDeviceAdapter;
    private LoadingDialog loadingDlg;
    private List<BindingDevice> currentBindingDeviceList = new ArrayList<>();
    private List<BindingDevice> historyBindingDeviceList = new ArrayList<>();

    @Override
    public void onCreate() {
        initView();
        getBindDeviceList();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.setting_device_manager_activity;
    }

    private void initView() {
        loadingDlg = new LoadingDialog(this);
        currentDeviceListView = (ListView) findViewById(R.id.current_device_list);
        historyDeviceListView = (ListView) findViewById(R.id.history_device_list);
        currentDeviceAdapter = new Adapter(true);
        historyDeviceAdapter = new Adapter(false);
        currentDeviceListView.setAdapter(currentDeviceAdapter);
        historyDeviceListView.setAdapter(historyDeviceAdapter);
        currentDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), DeviceInfoActivity.class);
                intent.putExtra("binding_device", currentBindingDeviceList.get(position));
                intent.putExtra("isCurrentBind", true);
                startActivityForResult(intent, UNBIND_DEVICE);
            }
        });
        historyDeviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), DeviceInfoActivity.class);
                intent.putExtra("binding_device", historyBindingDeviceList.get(position));
                intent.putExtra("isCurrentBind", false);
                startActivityForResult(intent, UNBIND_DEVICE);
            }
        });
    }

    /**
     * 获取设备绑定列表
     */
    private void getBindDeviceList() {
        if (NetUtils.isNetworkConnected(getApplicationContext())) {
            loadingDlg.show();
            SettingAPIService apiService = new SettingAPIService(DeviceManagerActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.getBindingDeviceList();
        }
    }


    public void onClick(View v) {
        if (v.getId() == R.id.ibt_back) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UNBIND_DEVICE && resultCode == RESULT_OK) {
            BindingDevice bindingDevice = (BindingDevice) data.getSerializableExtra("binding_device");
            currentBindingDeviceList.remove(bindingDevice);
            currentDeviceAdapter.notifyDataSetChanged();
        }
    }

    private class Adapter extends BaseAdapter {
        private boolean isCurrent;

        public Adapter(boolean isCurrent) {
            this.isCurrent = isCurrent;
        }

        @Override
        public int getCount() {
            return isCurrent ? currentBindingDeviceList.size() : historyBindingDeviceList.size();
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
            BindingDevice bindingDevice = isCurrent ? currentBindingDeviceList.get(position) : historyBindingDeviceList.get(position);
            convertView = LayoutInflater.from(DeviceManagerActivity.this).inflate(R.layout.setting_mine_setting_binding_devcie_list_item, null);
            ((TextView) convertView.findViewById(R.id.device_text)).setText(bindingDevice.getDeviceModel());
            String deviceLastUserTime = TimeUtils.getTime(bindingDevice.getDeviceLastUserTime(), TimeUtils.getFormat(DeviceManagerActivity.this, TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));

            ((TextView) convertView.findViewById(R.id.device_last_use_time_text)).setText(deviceLastUserTime);
            if (bindingDevice.getDeviceId().equals(AppUtils.getMyUUID(DeviceManagerActivity.this))) {
                (convertView.findViewById(R.id.current_device_text)).setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }

    private class WebService extends SettingAPIInterfaceImpl {
        @Override
        public void returnBindingDeviceListSuccess(GetBindingDeviceResult getBindingDeviceResult) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            currentBindingDeviceList = getBindingDeviceResult.getCurrentDeviceList();
            historyBindingDeviceList = getBindingDeviceResult.getHistoryDeviceList();
            if (currentBindingDeviceList.size() > 0) {
                findViewById(R.id.current_text).setVisibility(View.VISIBLE);
            }
            if (historyBindingDeviceList.size() > 0) {
                findViewById(R.id.history_text).setVisibility(View.VISIBLE);
            }
            currentDeviceAdapter.notifyDataSetChanged();
            historyDeviceAdapter.notifyDataSetChanged();
        }

        @Override
        public void returnBindingDeviceListFail(String error, int errorCode) {
            if (loadingDlg != null && loadingDlg.isShowing()) {
                loadingDlg.dismiss();
            }
            WebServiceMiddleUtils.hand(DeviceManagerActivity.this, error, errorCode);
        }
    }
}
