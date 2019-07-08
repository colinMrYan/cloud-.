package com.inspur.emmcloud.login.util.MDM;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;

import com.alibaba.android.arouter.launcher.ARouter;
import com.inspur.emmcloud.baselib.util.ToastUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.basemodule.util.Res;
import com.inspur.emmcloud.basemodule.util.WebServiceMiddleUtils;
import com.inspur.emmcloud.login.api.LoginAPIInterfaceImpl;
import com.inspur.emmcloud.login.api.LoginAPIService;
import com.inspur.emmcloud.login.api.LoginAPIUri;
import com.inspur.emmcloud.login.bean.GetDeviceCheckResult;
import com.inspur.emmcloud.login.ui.DeviceRegisterFailDetailActivity;

import java.util.ArrayList;

public class MDM extends LoginAPIInterfaceImpl {
    private static final int STATUS_NOT_REGISTERED = 0;
    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_DISABLE = 2;
    private static final int STATUS_WAITING_VERIFY = 3;
    private static final int STATUS_REVIEW_REJECT = 4;
    private static final int STATUS_IN_BLACKLIST = 5;
    private static MDMListener mdmListener;
    private Activity context;
    private GetDeviceCheckResult getDeviceCheckResult;
    private String tanentId;
    private String userCode;
    private String userName;
    private ArrayList<String> requireFieldList;
    private boolean isImpActivity = false;//判断是否是ImpActivity调用的MDM，此页面进行特殊处理

    public MDM() {

    }

    public MDM(Activity context, String tanentId, String userCode,
               String userName) {
        this.context = context;
        this.tanentId = tanentId;
        this.userCode = userCode;
        this.userName = userName;
    }

    public MDM(Activity context, String tanentId, String userCode,
               String userName, String state) {
        this.userName = userName;
        this.context = context;
        this.tanentId = tanentId;
        this.userCode = userCode;
        this.getDeviceCheckResult = new GetDeviceCheckResult(state);
    }

    public void setImpActivity(boolean impActivity) {
        isImpActivity = impActivity;
    }

    public void addOnMDMListener(MDMListener mdmListener) {
        MDM.mdmListener = mdmListener;
    }

    public void destroyOnMDMListener() {
        mdmListener = null;
    }

    public MDMListener getMDMListener() {
        return mdmListener;
    }

    /**
     * 处理设备检查结果
     **/
    public void handCheckResult() {
        // TODO Auto-generated method stub
        int deviceStatus = getDeviceCheckResult.getState();
        switch (deviceStatus) {
            case STATUS_NORMAL:
                if (mdmListener != null) {
                    mdmListener.MDMStatusPass(getDeviceCheckResult.getDoubleValidation());
                }
                if (isImpActivity) {
                    context.finish();
                }
                break;
            case STATUS_DISABLE:
                showWraningDlg(STATUS_DISABLE);
                break;
            case STATUS_WAITING_VERIFY:
                showWraningDlg(STATUS_WAITING_VERIFY);
                break;
            case STATUS_NOT_REGISTERED:
                goDeviceRegister();
            ToastUtils.show(context, Res.getStringID("device_not_register"));
                break;
            case STATUS_IN_BLACKLIST:
                showWraningDlg(STATUS_IN_BLACKLIST);
                break;
            case STATUS_REVIEW_REJECT:
                goDeviceRegisterFailDetail();
                break;
            default:
                break;
        }
    }

    /**
     * 跳转到设备注册页面
     */
    private void goDeviceRegister() {
        // TODO Auto-generated method stub
        if (mdmListener != null) {
            mdmListener.dimissExternalLoadingDlg();
        }
        Bundle bundle = new Bundle();
        bundle.putString("appName", context.getString(Res.getStringID("device_registe")));
        bundle.putString("function", "mdm");
        bundle.putString("uri", LoginAPIUri.getDeviceRegisterUrl(context));
        ARouter.getInstance().build(Constant.AROUTER_CLASS_WEB_MAIN).with(bundle).navigation();
    }

    private void goDeviceRegisterFailDetail() {
        // TODO Auto-generated method stub
        if (mdmListener != null) {
            mdmListener.dimissExternalLoadingDlg();
        }
        Intent intent = new Intent();
        intent.setClass(context, DeviceRegisterFailDetailActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("userCode", userCode);
        bundle.putString("tanentId", tanentId);
        bundle.putString("userName", userName);
        bundle.putString("message", getDeviceCheckResult.getMessage());
        bundle.putStringArrayList("requireFields", requireFieldList);
        bundle.putString("uri", LoginAPIUri.getDeviceRegisterUrl(context));
        intent.putExtra("bundle", bundle);
        context.startActivity(intent);
        if (isImpActivity) {
            context.finish();
        }
    }

    /**
     * 弹出提示框（设备被禁用或设备正在审核中）
     *
     * @param status
     */
    private void showWraningDlg(final int status) {
        String title = "";
        if (status == STATUS_DISABLE) {
            title = context.getString(Res.getStringID("device_disabled_cannot_login"));
        } else if (status == STATUS_WAITING_VERIFY) {
            if (isImpActivity) {
                title = context
                        .getString(Res.getStringID("register_submit_waitting_verify"));
            } else {
                title = context.getString(Res.getStringID("device_waitting_verify"));
            }
        } else {
            title = context.getString(Res.getStringID("device_in_blacklist"));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context,
                android.R.style.Theme_Holo_Light_Dialog);

        builder.setTitle(context.getString(Res.getStringID("mdm_tips")));
        builder.setMessage(title);
        builder.setPositiveButton(context.getString(Res.getStringID("mdm_sure")),
                new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        if (isImpActivity) {
                            context.finish();
                        }
                        if (mdmListener != null) {
                            mdmListener.MDMStatusNoPass();
                        }
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(
                android.R.color.transparent);
        dialog.setCancelable(false);
        dialog.show();
    }


    /**
     * 设备检查
     */
    public void deviceCheck() {
        if (NetUtils.isNetworkConnected(context)) {
            LoginAPIService apiServices = new LoginAPIService(context);
            apiServices.setAPIInterface(MDM.this);
            apiServices.deviceCheck(tanentId, userCode);
        } else if (mdmListener != null) {
            mdmListener.MDMStatusNoPass();
        }
    }

    @Override
    public void returnDeviceCheckSuccess(
            GetDeviceCheckResult getDeviceCheckResult) {
        // TODO Auto-generated method stub
        this.getDeviceCheckResult = getDeviceCheckResult;
        requireFieldList = getDeviceCheckResult.getRequiredFieldList();
        handCheckResult();
    }

    @Override
    public void returnDeviceCheckFail(String error, int errorCode) {
        // TODO Auto-generated method stub
        if (mdmListener != null) {
            mdmListener.MDMStatusNoPass();
        }
        WebServiceMiddleUtils.hand(context, error, errorCode);
    }

}
