package com.inspur.imp.api;

import android.content.Intent;

import java.util.Map;

/**
 * Created by yufuchang on 2018/7/11.
 */

public interface ImpCallBackInterface {
    void onDialogDissmiss();
    void onShowImpDialog();
    Map<String, String> onGetWebViewHeaders();
    void onInitWebViewGoBackOrClose();
    void onSetTitle();
    void onFinishActivity();
    void onShowLoadingDlg();
    void onStartActivityForResult(Intent intent,int requestCode);
}
