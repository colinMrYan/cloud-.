package com.inspur.imp.plugin.broadcast;

import com.inspur.imp.plugin.ImpPlugin;

import org.json.JSONObject;

/**
 * Created by chenmch on 2017/7/19.
 */

public class BroadcastService extends ImpPlugin {
    @Override
    public void execute(String action, JSONObject paramsObject) {
        if ("send".equals(action)) {

        }else if ("receive".equals(action)) {
        }

    }

    @Override
    public String executeAndReturn(String action, JSONObject paramsObject) {
        return super.executeAndReturn(action, paramsObject);
    }

    @Override
    public void onActivityResume() {
        super.onActivityResume();
    }

    @Override
    public void onActivityPause() {
        super.onActivityPause();
    }

    @Override
    public void onDestroy() {

    }
}

