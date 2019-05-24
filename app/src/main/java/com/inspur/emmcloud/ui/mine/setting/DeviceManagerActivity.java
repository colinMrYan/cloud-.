package com.inspur.emmcloud.ui.mine.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspur.emmcloud.BaseActivity;
import com.inspur.emmcloud.R;
import com.inspur.emmcloud.api.APIInterfaceInstance;
import com.inspur.emmcloud.api.apiservice.MineAPIService;
import com.inspur.emmcloud.bean.mine.BindingDevice;
import com.inspur.emmcloud.bean.mine.GetBindingDeviceResult;
import com.inspur.emmcloud.util.common.NetUtils;
import com.inspur.emmcloud.util.privates.AppUtils;
import com.inspur.emmcloud.util.privates.TimeUtils;
import com.inspur.emmcloud.util.privates.WebServiceMiddleUtils;
import com.inspur.emmcloud.widget.LoadingDialog;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        getBindDeviceList();
    }

    @Override
    public int getLayoutResId() {
        return R.layout.activity_setting_device_manager;
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
            MineAPIService apiService = new MineAPIService(DeviceManagerActivity.this);
            apiService.setAPIInterface(new WebService());
            apiService.getBindingDeviceList();
        }
    }


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibt_back:
                finish();
                break;
            default:
                break;
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
            convertView = LayoutInflater.from(DeviceManagerActivity.this).inflate(R.layout.mine_setting_binding_devcie_list_item, null);
            ((TextView) convertView.findViewById(R.id.device_text)).setText(bindingDevice.getDeviceModel());
            String deviceLastUserTime = TimeUtils.getTime(bindingDevice.getDeviceLastUserTime(), TimeUtils.getFormat(DeviceManagerActivity.this, TimeUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE));

            ((TextView) convertView.findViewById(R.id.device_last_use_time_text)).setText(deviceLastUserTime);
            if (bindingDevice.getDeviceId().equals(AppUtils.getMyUUID(DeviceManagerActivity.this))) {
                (convertView.findViewById(R.id.current_device_text)).setVisibility(View.VISIBLE);
            }
            return convertView;
        }
    }

    private class WebService extends APIInterfaceInstance {
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
