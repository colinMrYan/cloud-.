package com.inspur.emmcloud.bean;

import com.inspur.emmcloud.util.JSONUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenmch on 2017/10/10.
 */

public class GetAppConfigResult {
    private boolean isWebAutoRotate = false;
    private List<String> commonFunctionAppIDList= new ArrayList<>();
    public GetAppConfigResult(String response){
        JSONObject obj = JSONUtils.getJSONObject(response);
        isWebAutoRotate = JSONUtils.getBoolean(obj,"WebAutoRotate",false);
        commonFunctionAppIDList = JSONUtils.getStringList(obj,"CommonFunctions",new ArrayList<String>());
    }

    public List<String> getCommonFunctionAppIDList() {
        return commonFunctionAppIDList;
    }

    public void setCommonFunctionAppIDList(List<String> commonFunctionAppIDList) {
        this.commonFunctionAppIDList = commonFunctionAppIDList;
    }
}
