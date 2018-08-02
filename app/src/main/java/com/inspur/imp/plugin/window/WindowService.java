package com.inspur.imp.plugin.window;

import com.inspur.emmcloud.util.common.JSONUtils;
import com.inspur.emmcloud.util.privates.UriUtils;
import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yufuchang on 2018/7/20.
 */

public class WindowService extends ImpPlugin {
    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("open".equals(action)) {
            UriUtils.openUrl(getActivity(), JSONUtils.getString(paramsObject, "url", ""), JSONUtils.getString(paramsObject, "title", ""));
        } else if ("setTitles".equals(action)) {
            showDropTitle(paramsObject);
        } else {
            showCallIMPMethodErrorDlg();
        }
    }

    private void showDropTitle(JSONObject paramsObject){
        List<DropItemTitle> dropItemTitleList = new ArrayList<>();
        JSONArray array = JSONUtils.getJSONArray(paramsObject,"config",new JSONArray());
        for (int i=0;i<array.length();i++){
            dropItemTitleList.add(new DropItemTitle(JSONUtils.getJSONObject(array,i,new JSONObject())));
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
