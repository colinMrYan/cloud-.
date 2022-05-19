package com.inspur.emmcloud.web.plugin.nfc;

import static com.inspur.emmcloud.basemodule.util.PreferencesByUsersUtils.bytesToHexString;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.os.Parcelable;
import android.provider.Settings;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.LogUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.baselib.widget.dialogs.CustomDialog;
import com.inspur.emmcloud.web.R;
import com.inspur.emmcloud.web.plugin.ImpPlugin;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by chenmch on 2019/8/7.
 */

public class NFCService extends ImpPlugin {
    public static IntentFilter[] mIntentFilter = null;
    public String successCb, failCb; // 回调方法
    private boolean isListenActivityStart = false;
    private PendingIntent mPendingIntent;
    private NfcAdapter NFCAdapter;
    private String[][] mTechList;

    public void getNdefMsg(Intent intent) {
        if (intent == null)
            return;
        //nfc卡支持的格式
        String content = "";
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        String[] temp = tag.getTechList();

        content +="卡片字节数组ID："+tag.getId()+"<br/>";
        content +="卡片16进制ID："+ bytesToHexString(tag.getId())+"<br/>";
//        String tagid = reverseTwo(bytesToHexString(tag.getId()).split(","));
//        content +="卡片16进制翻转ID："+tagid+"<br/>";
//        content +="卡片10进制卡号："+Integer.parseInt(tagid, 16)+"<br/>";

        for (String s : temp) {
            LogUtils.jasonDebug("resolveIntent tag: " + s);
        }
        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) ||
                NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Parcelable[] rawMessage = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] ndefMessages = null;

            // 判断是哪种类型的数据 默认为NDEF格式
            if (rawMessage != null) {
                LogUtils.jasonDebug("getNdefMsg: ndef格式 ");
                ndefMessages = new NdefMessage[rawMessage.length];
                for (int i = 0; i < rawMessage.length; i++) {
                    ndefMessages[i] = (NdefMessage) rawMessage[i];
                }
                String ndefInfo = "";
                for (NdefMessage message : ndefMessages){
                    ndefInfo += message.toString();
                }
                callbackSuccess(ndefInfo);
            } else {
                //未知类型 (公交卡类型)
                LogUtils.jasonDebug("getNdefMsg: 未知类型");
                if (StringUtils.isEmpty(content)){
                    callbackFail("getNdefMsg: 未知类型");
                } else {
                    callbackSuccess(content);
                }
                //对应的解析操作，在Github上有
            }
        }
    }

    private static String reverseTwo(String[] str) {
        String str1 = "";
        for (int i = 1; i <= str.length; i++) {
            str1 += str[i - 1];
            if (i % 2 == 0) {
                if (i == str.length) {
                    break;
                }
                str1 += ":";
            }
        }
        String str2 = "";
        for (int i = str1.split(":").length - 1; i >= 0; i--) {
            str2 += str1.split(":")[i];
        }
        return str2;
    }

    /*
     * 解析NDEF文本数据，从第三个字节开始，后面的文本数据
     * @param ndefRecord
     * @return
     */
    private static String parseTextRecord(NdefRecord ndefRecord) {
        /**
         * 判断数据是否为NDEF格式
         */
        //判断TNF
        if (ndefRecord.getTnf() != NdefRecord.TNF_WELL_KNOWN) {
            return null;
        }
        //判断可变的长度的类型
        if (!Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
            return null;
        }
        try {
            //获得字节数组，然后进行分析
            byte[] payload = ndefRecord.getPayload();
            //下面开始NDEF文本数据第一个字节，状态字节
            //判断文本是基于UTF-8还是UTF-16的，取第一个字节"位与"上16进制的80，16进制的80也就是最高位是1，
            //其他位都是0，所以进行"位与"运算后就会保留最高位
            String textEncoding = ((payload[0] & 0x80) == 0) ? "UTF-8" : "UTF-16";
            //3f最高两位是0，第六位是1，所以进行"位与"运算后获得第六位
            int languageCodeLength = payload[0] & 0x3f;
            //下面开始NDEF文本数据第二个字节，语言编码
            //获得语言编码
            String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            //下面开始NDEF文本数据后面的字节，解析出文本
            String textRecord = new String(payload, languageCodeLength + 1,
                    payload.length - languageCodeLength - 1, textEncoding);
            return textRecord;
        } catch (Exception e) {
            throw new IllegalArgumentException();
        }
    }

    private void readNfcTag(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawArray = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawArray == null || rawArray.length == 0) return;
            NdefMessage mNdefMsg = (NdefMessage) rawArray[0];
            NdefRecord mNdefRecord = mNdefMsg.getRecords()[0];
            try {
                if (mNdefRecord != null) {
                    String readResult = new String(mNdefRecord.getPayload(), "UTF-8");
//                    LogUtils.jasonDebug("readResult===" + readResult);
//                    String readResult  = parseTextRecord(mNdefRecord);
                    callbackSuccess(readResult);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

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
    public void onActivityResume() {
        super.onActivityStart();
        if (isListenActivityStart) {
            checkNFCStatus();
            isListenActivityStart = false;
        } else {
            if (NFCAdapter != null) {
                NFCAdapter.enableForegroundDispatch(getActivity(), mPendingIntent, mIntentFilter, mTechList);
            }
        }
    }

    @Override
    public void onActivityNewIntent(Intent intent) {
        super.onActivityNewIntent(intent);
        if (NFCAdapter != null) { //有nfc功能
            if (NFCAdapter.isEnabled()) {//nfc功能打开了
                getNdefMsg(intent);
            }
        }
    }


    private void callbackSuccess(String result) {
        if (!StringUtils.isBlank(successCb)) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("result", result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.jsCallback(successCb, obj.toString());
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
        NFCAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        if (NFCAdapter == null) {
            callbackFail("设备不支持NFC功能");
        } else if (!NFCAdapter.isEnabled()) {
            showToSetNFCDlg();
        } else {
            initNFC();
        }
    }


    private void initNFC() {
        LogUtils.jasonDebug("initNFC==========");
        Intent intent = new Intent(getActivity(), getActivity().getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mPendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, 0);
        //做一个IntentFilter过滤你想要的action 这里过滤的是ndef
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter filter2 = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);

        try {
            filter.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }
        //生成intentFilter
        mIntentFilter = new IntentFilter[]{filter, filter2};
        mTechList = new String[][]{{MifareClassic.class.getName()}, {NfcA.class.getName()}, {NfcB.class.getName()}};
        NFCAdapter.enableForegroundDispatch(getActivity(), mPendingIntent, mIntentFilter, mTechList);
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
                        getActivity().startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                        isListenActivityStart = true;
                    }
                })
                .show();
    }

    @Override
    public void onActivityPause() {
        if (NFCAdapter != null) {
            NFCAdapter.disableForegroundDispatch(getActivity());
        }

    }

    @Override
    public void onDestroy() {
        if (NFCAdapter != null) {
            NFCAdapter.disableForegroundDispatch(getActivity());
        }
    }
}
