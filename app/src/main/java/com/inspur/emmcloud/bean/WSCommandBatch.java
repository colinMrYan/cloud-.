package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;
import com.inspur.emmcloud.bean.chat.WSCommand;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2019/10/28.
 * 批量命令消息处理
 */

public class WSCommandBatch {
    private String tracer;
    private String request;
    private List<WSCommand> wsCommandList = new ArrayList<>();

    public WSCommandBatch(String request) {
        this.request = request;
        JSONObject requestObj = JSONUtils.getJSONObject(request);
        JSONObject headerObj = JSONUtils.getJSONObject(requestObj, "headers", new JSONObject());
        tracer = JSONUtils.getString(headerObj, "tracer", "");
        JSONArray array = JSONUtils.getJSONArray(requestObj, "body", new JSONArray());
        for (int i = 0; i < array.length(); i++) {
            String wsCommandJson = JSONUtils.getString(array, i, "");
            WSCommand wsCommand = new WSCommand(wsCommandJson);
            wsCommandList.add(wsCommand);
        }
    }

    public String getTracer() {
        return tracer;
    }

    public void setTracer(String tracer) {
        this.tracer = tracer;
    }

    public List<WSCommand> getWsCommandList() {
        return wsCommandList;
    }

    public void setWsCommandList(List<WSCommand> wsCommandList) {
        this.wsCommandList = wsCommandList;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
