package com.inspur.emmcloud.web.plugin.window;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.basemodule.util.NetUtils;
import com.inspur.emmcloud.componentservice.application.maintab.MainTabMenu;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.ui.ImpActivity;
import com.inspur.emmcloud.web.ui.ImpFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * Created by yufuchang on 2018/7/20.
 */

public class WindowService extends ImpPlugin implements OnKeyDownListener, OnTitleBackKeyDownListener {

    private String onBackKeyDownCallback;
    private String onTitleBackKeyDownCallback;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        switch (action) {
            case "open":
                open(paramsObject);
                break;
            case "setTitles":
                showDropTitle(paramsObject);
                break;
            case "setMenus":
                showMenus(paramsObject);
                break;
            case "onBackKeyDown":
                onBackKeyDown(paramsObject);
                break;
            case "onTitleBackKeyDown":
                onTitleBackKeyDown(paramsObject);
                break;
            default:
                showCallIMPMethodErrorDlg();
                break;
        }
    }

    private void open(JSONObject paramsObject) {
        String uri = JSONUtils.getString(paramsObject, "uri", "");
        if (!TextUtils.isEmpty(uri)) {
            openUri(uri);
        } else {
            openUrl(paramsObject);
        }
    }

    private void openUri(String uri) {
        try {
            Intent intent = Intent.parseUri(uri, Intent.URI_INTENT_SCHEME);
            intent.setComponent(null);
            getActivity().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openUrl(JSONObject paramsObject) {
        String url = JSONUtils.getString(paramsObject, "url", "");
        String title = JSONUtils.getString(paramsObject, "title", "");
        String description = JSONUtils.getString(paramsObject, "description", "");
        boolean isHaveNavBar = JSONUtils.getBoolean(paramsObject, "isHaveNavbar", true);
        boolean isShare = JSONUtils.getBoolean(paramsObject, "isShare", false);
        boolean isHaveAPPNavBar = JSONUtils.getBoolean(paramsObject, "isHaveAPPNavbar", true);
        String appName = JSONUtils.getString(paramsObject, "app_name", "");
        String ico = JSONUtils.getString(paramsObject, "ico", "");
        String appUrl = JSONUtils.getString(paramsObject, "app_url", "");

        Bundle bundle = new Bundle();
        bundle.putString("uri", url);
        bundle.putString("appName", title);
        bundle.putBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, isHaveNavBar);
        if (isShare) {
            bundle.putBoolean("isShare", true);
            bundle.putBoolean("isHaveAPPNavbar", isHaveAPPNavBar);
            bundle.putString("description", description);
            bundle.putString("app_name", appName);
            bundle.putString("app_url", appUrl);
            bundle.putString("ico", ico);
            if (getImpCallBackInterface() != null) {
                getImpCallBackInterface().onStartActivityForResult(Constant.AROUTER_CLASS_CONVERSATION_SEARCH, bundle, ImpFragment.SHARE_WEB_URL_REQUEST);
            }
        } else {

            Intent intent = new Intent(getActivity(), ImpActivity.class);
            intent.putExtras(bundle);
            getActivity().startActivity(intent);
        }
    }

    private void onBackKeyDown(JSONObject paramsObject) {
        onBackKeyDownCallback = JSONUtils.getString(paramsObject, "callback", "");
        if (getImpCallBackInterface() != null) {
            if (TextUtils.isEmpty(onBackKeyDownCallback)) {
                getImpCallBackInterface().setOnKeyDownListener(null);
            } else {
                getImpCallBackInterface().setOnKeyDownListener(WindowService.this);
            }
        }
    }

    private void onTitleBackKeyDown(JSONObject paramsObject) {
        onTitleBackKeyDownCallback = JSONUtils.getString(paramsObject, "callback", "");
        if (getImpCallBackInterface() != null) {
            if (TextUtils.isEmpty(onTitleBackKeyDownCallback)) {
                getImpCallBackInterface().setOnTitleBackKeyDownListener(null);
            } else {
                getImpCallBackInterface().setOnTitleBackKeyDownListener(WindowService.this);

            }
        }
    }

    @Override
    public void onBackKeyDown() {
        if (!StringUtils.isBlank(onBackKeyDownCallback)) {
            WindowService.this.jsCallback(onBackKeyDownCallback);
        }
    }

    @Override
    public void onTitleBackKeyDown() {
        if (!StringUtils.isBlank(onTitleBackKeyDownCallback)) {
            WindowService.this.jsCallback(onTitleBackKeyDownCallback);
        }
    }

    private void showDropTitle(JSONObject paramsObject) {
        List<DropItemTitle> dropItemTitleList = new ArrayList<>();
        JSONArray array = JSONUtils.getJSONArray(paramsObject, "config", new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            dropItemTitleList.add(new DropItemTitle(JSONUtils.getJSONObject(array, i, new JSONObject())));
        }
        if (dropItemTitleList.size() > 0 && getImpCallBackInterface() != null) {
            getImpCallBackInterface().onSetDropTitles(dropItemTitleList);
        }
    }

    private void showMenus(JSONObject paramsObject) {
        List<MainTabMenu> optionMenuList = new ArrayList<>();
        JSONArray array = JSONUtils.getJSONArray(paramsObject, "menus", new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            optionMenuList.add(new MainTabMenu(JSONUtils.getJSONObject(array, i, new JSONObject())));
        }
        if (optionMenuList.size() > 0 && getImpCallBackInterface() != null) {
            getImpCallBackInterface().onSetOptionMenu(optionMenuList);
        }
    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        showCallIMPMethodErrorDlg();
        return "";
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImpFragment.SHARE_WEB_URL_REQUEST && resultCode == RESULT_OK
                && NetUtils.isNetworkConnected(getFragmentContext())) {
        }
    }
}
