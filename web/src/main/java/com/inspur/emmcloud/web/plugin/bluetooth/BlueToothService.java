package com.inspur.emmcloud.web.plugin.bluetooth;

import static com.inspur.emmcloud.web.ui.ImpFragment.REQUEST_CONNECT_DEVICE_SECURE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.AppUtils;
import com.inspur.emmcloud.basemodule.util.systool.emmpermission.Permissions;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestCallback;
import com.inspur.emmcloud.basemodule.util.systool.permission.PermissionRequestManagerUtils;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@SuppressLint("MissingPermission")
public class BlueToothService extends ImpPlugin {
    private String successCal, failCal, updateCal;
    HashMap<String, String> discoveryDevicesMap = new HashMap<>();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //dev_mac_adress.contains(device.getAddress())避免重复添加
                if (device.getName() != null && discoveryDevicesMap.get(device.getName()) == null) {
                    discoveryDevicesMap.put(device.getName(), device.getAddress());
                }
                try {
                    JSONObject jsonObject = JSONUtils.map2Json(discoveryDevicesMap);
                    JSONObject json = new JSONObject();
                    json.put("state", 1);
                    JSONObject result = new JSONObject();
                    result.put("data", jsonObject);
                    json.put("result", result);
                    jsCallback(successCal, jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                // no device find
            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (blueState){
                    case BluetoothAdapter.STATE_OFF:
                        Toast.makeText(context , "蓝牙已关闭", Toast.LENGTH_SHORT).show();
                        if (updateCal != null && updateState){
                            try {
                                JSONObject json = new JSONObject();
                                json.put("state", 1);
                                JSONObject result = new JSONObject();
                                result.put("data", 0);
                                json.put("result", result);
                                jsCallback(updateCal, json);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Toast.makeText(context , "蓝牙已开启"  , Toast.LENGTH_SHORT).show();
                        if (updateCal != null && updateState) {
                            try {
                                JSONObject json = new JSONObject();
                                json.put("state", 1);
                                JSONObject result = new JSONObject();
                                result.put("data", 1);
                                json.put("result", result);
                                jsCallback(updateCal, json);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        }
    };
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constant.BLUETOOTH_MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            break;
                    }
                    break;
                case Constant.BLUETOOTH_MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    break;
                case Constant.BLUETOOTH_MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    break;
                case Constant.BLUETOOTH_MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String mConnectedDeviceName = msg.getData().getString(Constant.BLUETOOTH_DEVICE_NAME);
                    break;
                case Constant.BLUETOOTH_MESSAGE_TOAST:
                    if (null != getActivity()) {
                        Toast.makeText(getActivity(), msg.getData().getString(Constant.BLUETOOTH_TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
    private BluetoothChatService mChatService;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean updateState = true;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        initBlueToothService(paramsObject, action.equals("updateState"));
        switch (action) {
            case "open":
                openBluetooth();
                break;
            case "close":
                closeBluetooth();
                break;
            case "getState":
                getBluetoothState();
                break;
            case "scan":
                scanBluetooth();
                break;
            case "cancelScan":
                cancelDiscovery();
                break;
            case "connectDevice":
                connectDevice(paramsObject);
                break;
            case "updateState":
                updateBluetoothState(paramsObject);
                break;
            default:
                showCallIMPMethodErrorDlg();
                break;
        }

    }

    private void openBluetooth() {
        if (mBluetoothAdapter == null) {
            jsCallback(failCal, "device not support bluetooth");
            return;
        }
        if (mBluetoothAdapter.isEnabled() || mBluetoothAdapter.enable()) {
            JSONObject json = new JSONObject();
            JSONObject result = new JSONObject();
            try {
                json.put("state", 1);
                json.put("result", result);
                jsCallback(successCal, json);
            } catch (JSONException e) {
                e.printStackTrace();
                jsCallback(failCal, e.getMessage());
            }
        } else {
            jsCallback(failCal, "open bluetooth fail");
        }
    }

    private void initBlueToothService(JSONObject paramsObject,boolean needUpdate) {
        if (needUpdate) {
            updateCal = JSONUtils.getString(paramsObject, "success", "");
        } else {
            successCal = JSONUtils.getString(paramsObject, "success", "");
        }
        failCal = JSONUtils.getString(paramsObject, "fail", "");
        if (mChatService == null) mChatService = new BluetoothChatService(getActivity(), mHandler);
        if (mBluetoothAdapter == null) mBluetoothAdapter = mChatService.getBluetoothAdapter();
        // Register for broadcasts when a device is discovered
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(mReceiver, intent);
    }

    private void closeBluetooth() {
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                mChatService.stop();
                if (mBluetoothAdapter.disable() && !StringUtils.isEmpty(successCal)) {
                    JSONObject json = new JSONObject();
                    JSONObject result = new JSONObject();
                    try {
                        json.put("state", 1);
                        json.put("result", result);
                        jsCallback(successCal, json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        jsCallback(failCal, e.getMessage());
                    }
                } else {
                    jsCallback(failCal, "blueTooth closed already");
                }
            }
        } else {
            jsCallback(failCal, "device not support bluetooth");
        }
    }

    private void getBluetoothState() {
        if (mBluetoothAdapter != null && !StringUtils.isEmpty(successCal)) {
            JSONObject json = new JSONObject();
            JSONObject result = new JSONObject();
            try {
                json.put("state", 1);
                result.put("data",mBluetoothAdapter.isEnabled() ? 1 : 0);
                json.put("result", result);
                jsCallback(successCal, json);
            } catch (JSONException e) {
                e.printStackTrace();
                jsCallback(failCal, e.getMessage());
            }
        } else {
            jsCallback(failCal, "device not support bluetooth");
        }
    }

    private void enableDiscovery() {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        getActivity().startActivity(discoverableIntent);
    }

    private void updateBluetoothState(JSONObject paramsObject) {
        try {
            final JSONObject optionsObj = paramsObject.getJSONObject("options");
            updateState = optionsObj.optBoolean("value", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void scanBluetooth() {
        PermissionRequestManagerUtils.getInstance().requestRuntimePermission(getActivity(), Permissions.BLUETOOTH, new PermissionRequestCallback() {
            @Override
            public void onPermissionRequestSuccess(List<String> permissions) {
                if (AppUtils.isLocationEnabled(getActivity())) {
                    if (isDiscovering()) {
                        return;
                    }
                    discoveryDevicesMap.clear();
                    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
                    for (BluetoothDevice bluetoothDevice : pairedDevices) {
                        discoveryDevicesMap.put(bluetoothDevice.getName(), bluetoothDevice.getAddress());
                    }
                    if (!isDiscovering()) {
                        startDiscovery();
                    }
                } else {
                    new CustomDialog.MessageDialogBuilder(getActivity())
                            .setMessage(getActivity().getString(R.string.imp_location_enable, AppUtils.getAppName(getFragmentContext())))
                            .setCancelable(false)
                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton(R.string.go_setting, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    AppUtils.openLocationSetting(getActivity());
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }

            @Override
            public void onPermissionRequestFail(List<String> permissions) {
                ToastUtils.show(getFragmentContext(), PermissionRequestManagerUtils.getInstance().getPermissionToast(getFragmentContext(), permissions));
            }

        });
    }

    /**
     * Establish connection with other device
     */
    private void connectDevice(JSONObject paramsObject) {
        JSONObject options = JSONUtils.getJSONObject(paramsObject, "options", new JSONObject());
        try {
            enableDiscovery();
            String deviceId = options.getString("value");
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceId);
            mChatService.connect(device, true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始搜索
     */
    public void startDiscovery() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.startDiscovery();
        }
    }

    /**
     * 取消搜索
     */
    public void cancelDiscovery() {
        if (isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
            JSONObject json = new JSONObject();
            try {
                json.put("state", 1);
                JSONObject result = new JSONObject();
                json.put("result", result);
                jsCallback(successCal, json);
            } catch (JSONException e) {
                e.printStackTrace();
                jsCallback(failCal, e.getMessage());
            }
        } else {
            JSONObject json = new JSONObject();
            try {
                //当前不在扫描状态
                json.put("state", 2);
                JSONObject result = new JSONObject();
                json.put("result", result);
                jsCallback(successCal, json);
            } catch (JSONException e) {
                e.printStackTrace();
                jsCallback(failCal, e.getMessage());
            }
        }
    }

    /**
     * 判断当前是否正在查找设备，是返回true
     */
    public boolean isDiscovering() {
        if (mBluetoothAdapter != null) {
            return mBluetoothAdapter.isDiscovering();
        }
        return false;
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return null;
    }

    @Override
    public void onActivityStart() {
        if (mChatService == null) mChatService = new BluetoothChatService(getActivity(), mHandler);
    }

    @Override
    public void onDestroy() {
        if (mChatService != null) {
            mChatService.stop();
        }
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {

                }
                break;
        }
    }
}
