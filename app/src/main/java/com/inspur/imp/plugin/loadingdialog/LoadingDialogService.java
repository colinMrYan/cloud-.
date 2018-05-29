package com.inspur.imp.plugin.loadingdialog;

import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.imp.api.ImpActivity;
import com.inspur.imp.plugin.ImpPlugin;
import com.inspur.imp.util.DialogUtil;

import org.json.JSONObject;


/**
 * web loading显示类
 *
 * @author 浪潮移动应用平台(IMP)产品组
 */
public class LoadingDialogService extends ImpPlugin {


    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("show".equals(action)) {
            showDlg(paramsObject);
        } else if ("hide".equals(action)) {
            hideDlg();
        }else{
            DialogUtil.getInstance(getActivity()).show();
        }
    }

    private void showDlg(JSONObject paramsObject) {
        String content = null;
        content = JSONUtils.getString(paramsObject, "content", "");
        ((ImpActivity) getActivity()).showLoadingDlg(content);
    }

    private void hideDlg() {
        ((ImpActivity) getActivity()).dimissLoadingDlg();
    }

    @Override
    public void onDestroy() {

    }
}
