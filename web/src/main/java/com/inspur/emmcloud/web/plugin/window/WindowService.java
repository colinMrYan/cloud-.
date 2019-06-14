package com.inspur.emmcloud.web.plugin.window;

import android.content.Intent;
import android.os.Bundle;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.baselib.util.StringUtils;
import com.inspur.emmcloud.basemodule.bean.MainTabMenu;
import com.inspur.emmcloud.basemodule.config.Constant;
import com.inspur.emmcloud.web.plugin.ImpPlugin;
import com.inspur.emmcloud.web.ui.ImpActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/7/20.
 */

public class WindowService extends ImpPlugin implements OnKeyDownListener {

    private String onBackKeyDownCallback;

    @Override
    public void execute(String action, JSONObject paramsObject) {
        switch (action) {
            case "open":
                openUrl(paramsObject);
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
            default:
                showCallIMPMethodErrorDlg();
                break;
        }
    }

    private void openUrl(JSONObject paramsObject) {
        String url = JSONUtils.getString(paramsObject, "url", "");
        String title = JSONUtils.getString(paramsObject, "title", "");
        boolean isHaveNavBar = JSONUtils.getBoolean(paramsObject, "isHaveNavbar", true);

        Bundle bundle = new Bundle();
        bundle.putString("uri", url);
        bundle.putString("appName", title);
        bundle.putBoolean(Constant.WEB_FRAGMENT_SHOW_HEADER, isHaveNavBar);
        Intent intent = new Intent(getActivity(), ImpActivity.class);
        intent.putExtras(bundle);
        getActivity().startActivity(intent);
    }

    private void onBackKeyDown(JSONObject paramsObject) {
        onBackKeyDownCallback = JSONUtils.getString(paramsObject, "callback", "");
        if (getImpCallBackInterface() != null) {
            getImpCallBackInterface().setOnKeyDownListener(WindowService.this);
        }
    }

    @Override
    public void onBackKeyDown() {
        if (!StringUtils.isBlank(onBackKeyDownCallback)) {
            WindowService.this.jsCallback(onBackKeyDownCallback);
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

}
