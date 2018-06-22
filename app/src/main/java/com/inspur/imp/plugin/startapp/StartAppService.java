/**
 * StartAppService.java
 * classes : com.inspur.imp.plugin.startapp.StartAppService
 * V 1.0.0
 * Create at 2016年9月18日 上午9:57:30
 */
package com.inspur.imp.plugin.startapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.common.StringUtils;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * com.inspur.imp.plugin.startapp.StartAppService create at 2016年9月18日 上午9:57:30
 */
public class StartAppService extends ImpPlugin {

    @Override
    public void execute(String action, JSONObject paramsObject) {
        // TODO Auto-generated method stub
        if ("open".equals(action)) {
            startApp(paramsObject);
        }
    }

    /**
     * 验证app是否已经安装
     *
     * @param packageName
     * @return
     */
    private boolean isAppInstall(String packageName) {
        PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }


    /**
     * 打开一个App
     *
     * @param paramsObject
     */
    private void startApp(JSONObject paramsObject) {
        // TODO Auto-generated method stub
        Bundle bundle = new Bundle();
        Intent intent = new Intent();
        String packageName = JSONUtils.getString(paramsObject, "packageName", null);
        String targetActivity = JSONUtils.getString(paramsObject, "activityName", null);
        String action = JSONUtils.getString(paramsObject, "action", null);
        String dataUri = JSONUtils.getString(paramsObject, "dataUri", null);
        String intentUri = JSONUtils.getString(paramsObject, "intentUri", null);
        JSONObject intentParmsObj = JSONUtils.getJSONObject(paramsObject, "intentParam", null);
        try {
            if (intentParmsObj != null) {
                Iterator<String> keys = intentParmsObj.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    bundle.putSerializable(key, (Serializable) intentParmsObj.get(key));
                }
            }
            if (!StringUtils.isBlank(packageName)) {
                if (!isAppInstall(packageName)) {
                    Toast.makeText(getActivity(), "应用未安装", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!StringUtils.isBlank(targetActivity)) {
                    ComponentName componet = new ComponentName(packageName,
                            targetActivity);
                    intent.setComponent(componet);
                } else {
                    intent = getActivity().getPackageManager().getLaunchIntentForPackage(packageName);
                }
            }


            if (!StringUtils.isBlank(intentUri)) {
                intent = Intent.parseUri(intentUri, 0);
            }

            if (!StringUtils.isBlank(dataUri)) {
                intent.setData(Uri.parse(dataUri));
            }
            if (!StringUtils.isBlank(action)) {
                intent.setAction(action);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtras(bundle);
            this.context.startActivity(intent);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            Toast.makeText(getActivity(), "应用打开失败", Toast.LENGTH_SHORT).show();
        }

    }


    /*
     * (non-Javadoc)
     *
     * @see com.inspur.imp.plugin.ImpPlugin#onDestroy()
     */
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

    }

}
