package com.inspur.emmcloud.web.plugin.nfc;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.provider.Settings;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONObject;

/**
 * Created by chenmch on 2019/8/7.
 */

public class NFCService extends ImpPlugin {
    public String successCb, failCb; // 回调方法
    private boolean isListenActivityStart = false;


    @Override
    public void execute(String action, JSONObject paramsObject) {
        if (action.equals("getNFCInfo")) {
            getNFCInfo(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return null;
    }

    @Override
    public void onActivityStart() {
        super.onActivityStart();
        if (isListenActivityStart) {
            checkNFCStatus();
        }
    }

    private void callbackSuccess() {
        if (!StringUtils.isBlank(successCb)) {
            this.jsCallback(successCb, "");
        }
    }

    private void callbackFail(String errorMessage) {
        if (!StringUtils.isBlank(failCb)) {
            this.jsCallback(failCb, errorMessage);
        }

    }

    private void getNFCInfo(JSONObject paramsObject) {
        successCb = JSONUtils.getString(paramsObject, "success", "");
        failCb = JSONUtils.getString(paramsObject, "fail", "");
        checkNFCStatus();
    }

    private void checkNFCStatus() {
        NfcAdapter NFCAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (NFCAdapter == null) {
            callbackFail("设备不支持NFC功能");
        } else if (!NFCAdapter.isEnabled()) {
            showToSetNFCDlg();
        } else {
            initNFC();
        }
    }


    private void initNFC() {
        Intent intent = new Intent(getActivity(), getActivity().getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);
        //做一个IntentFilter过滤你想要的action 这里过滤的是ndef
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            filter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        mTechList = new String[][]{{MifareClassic.class.getName()},
                {NfcA.class.getName()}};
        //生成intentFilter
        mIntentFilter = new IntentFilter[]{filter};
    }

    private void showToSetNFCDlg() {
        new CustomDialog.MessageDialogBuilder(getActivity())
                .setMessage("请开启NFC功能")
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        callbackFail("未开启NFC功能");
                    }
                })
                .setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        getActivity().startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                        isListenActivityStart = true;
                    }
                })
                .show();
    }

    @Override
    public void onDestroy() {
        isListenActivityStart = false;
    }
}
