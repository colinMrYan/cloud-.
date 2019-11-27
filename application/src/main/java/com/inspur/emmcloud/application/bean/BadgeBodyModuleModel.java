package com.inspur.emmcloud.application.bean;

import com.inspur.emmcloud.baselib.util.JSONUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by yufuchang on 2018/11/26.
 */

public class BadgeBodyModuleModel {

    /**
     * total : number
     * detail : {"123":"number"}
     */

    private int total;
    private Map<String, Integer> detailBodyMap = new HashMap<>();

    public BadgeBodyModuleModel(String bodyModule) {
        total = JSONUtils.getInt(bodyModule, "total", 0);
        JSONObject jsonObject = JSONUtils.getJSONObject(bodyModule, "detail", new JSONObject());
        Iterator<String> idIterator = jsonObject.keys();
        while (idIterator.hasNext()) {
            String key = String.valueOf(idIterator.next());
            Integer value = (Integer) jsonObject.optInt(key);
            detailBodyMap.put(key, value);
        }
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Map<String, Integer> getDetailBodyMap() {
        return detailBodyMap;
    }

    public void setDetailBodyMap(Map<String, Integer> detailBodyMap) {
        this.detailBodyMap = detailBodyMap;
    }
}
